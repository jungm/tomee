/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.jee.ManagedThreadFactory;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.PropertyPlaceHolderHelper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class ConvertManagedThreadFactoryDefinitions extends BaseConvertDefinitions {
    @Override
    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        final Map<String, ScopedDefinition<ManagedThreadFactory>> managedThreadFactories = new LinkedHashMap<>();
        final Map<String, ScopedDefinition<ManagedThreadFactory>> managedThreadFactoriesFromCompManagedBeans = new LinkedHashMap<>();

        for (final ScopedJndiConsumer scopedConsumer : collectScopedConsumers(appModule)) {
            final String moduleId = scopedConsumer.moduleId();
            final org.apache.openejb.jee.JndiConsumer consumer = scopedConsumer.consumer();
            if (consumer == null) {
                continue;
            }
            final Map<String, ScopedDefinition<ManagedThreadFactory>> target = consumer instanceof CompManagedBean
                    ? managedThreadFactoriesFromCompManagedBeans
                    : managedThreadFactories;
            for (final ManagedThreadFactory managedThreadFactory : consumer.getManagedThreadFactoryMap().values()) {
                /*
                 * TOMEE-2053: CompManagedBean may contain invalid definitions
                 * because it is never updated with content from ejb-jar.xml.
                 */
                target.put(scopedDefinitionKey(moduleId, managedThreadFactory.getName().getvalue()),
                        new ScopedDefinition<>(moduleId, managedThreadFactory));
            }
        }

        for (final Map.Entry<String, ScopedDefinition<ManagedThreadFactory>> entry : managedThreadFactoriesFromCompManagedBeans.entrySet()) {
            //Interested only in ManagedThreadFactoryServices that come from non-JndiConsumers
            if (!managedThreadFactories.containsKey(entry.getKey())) {
                managedThreadFactories.put(entry.getKey(), entry.getValue());
            }
        }

        for (final ScopedDefinition<ManagedThreadFactory> managedThreadFactory : managedThreadFactories.values()) {
            appModule.getResources().add(toResource(managedThreadFactory.definition(), managedThreadFactory.moduleId()));
        }

        return appModule;
    }

    private Resource toResource(final ManagedThreadFactory managedThreadFactory, final String moduleId) {
        validateQualifiedResourceName("ManagedThreadFactory", managedThreadFactory.getName().getvalue(), managedThreadFactory.getQualifiers());
        final String name = scopedResourceName(moduleId, managedThreadFactory.getName().getvalue());

        final Resource def = new Resource(name, jakarta.enterprise.concurrent.ManagedThreadFactory.class.getName());

        def.setJndi(managedThreadFactory.getName().getvalue().replaceFirst("java:", ""));

        final String contextName = resolveContextReference(moduleId, managedThreadFactory.getContextService());

        final Properties p = def.getProperties();
        put(p, "Context", contextName);
        put(p, "Priority", managedThreadFactory.getPriority());
        put(p, "Virtual", managedThreadFactory.isVirtual());
        put(p, "Qualifiers", Join.join(",", managedThreadFactory.getQualifiers()));

        // to force it to be bound in JndiEncBuilder
        put(p, "JndiName", def.getJndi());

        return def;
    }

    private static void put(final Properties properties, final String key, final Object value) {
        if (key == null) {
            return;
        }
        if (value == null) {
            return;
        }

        properties.put(key, PropertyPlaceHolderHelper.value(String.valueOf(value)));
    }

    private static final class ScopedDefinition<T> {
        private final String moduleId;
        private final T definition;

        private ScopedDefinition(final String moduleId, final T definition) {
            this.moduleId = moduleId;
            this.definition = definition;
        }

        private String moduleId() {
            return moduleId;
        }

        private T definition() {
            return definition;
        }
    }
}
