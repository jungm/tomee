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
import org.apache.openejb.jee.ManagedExecutor;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.PropertyPlaceHolderHelper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class ConvertManagedExecutorServiceDefinitions extends BaseConvertDefinitions {
    @Override
    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        final Map<String, ScopedDefinition<ManagedExecutor>> managedExecutors = new LinkedHashMap<>();
        final Map<String, ScopedDefinition<ManagedExecutor>> managedExecutorsFromCompManagedBeans = new LinkedHashMap<>();

        for (final ScopedJndiConsumer scopedConsumer : collectScopedConsumers(appModule)) {
            final String moduleId = scopedConsumer.moduleId();
            final org.apache.openejb.jee.JndiConsumer consumer = scopedConsumer.consumer();
            if (consumer == null) {
                continue;
            }
            final Map<String, ScopedDefinition<ManagedExecutor>> target = consumer instanceof CompManagedBean
                    ? managedExecutorsFromCompManagedBeans
                    : managedExecutors;
            for (final ManagedExecutor managedExecutor : consumer.getManagedExecutorMap().values()) {
                /*
                 * TOMEE-2053: CompManagedBean may contain invalid definitions
                 * because it is never updated with content from ejb-jar.xml.
                 */
                target.put(scopedDefinitionKey(moduleId, managedExecutor.getName().getvalue()),
                        new ScopedDefinition<>(moduleId, managedExecutor));
            }
        }

        for (final Map.Entry<String, ScopedDefinition<ManagedExecutor>> entry : managedExecutorsFromCompManagedBeans.entrySet()) {
            //Interested only in ManagedExecutorServices that come from non-JndiConsumers
            if (!managedExecutors.containsKey(entry.getKey())) {
                managedExecutors.put(entry.getKey(), entry.getValue());
            }
        }

        for (final ScopedDefinition<ManagedExecutor> managedExecutor : managedExecutors.values()) {
            appModule.getResources().add(toResource(managedExecutor.definition(), managedExecutor.moduleId()));
        }

        return appModule;
    }

    private Resource toResource(final ManagedExecutor managedExecutor, final String moduleId) {
        validateQualifiedResourceName("ManagedExecutorService", managedExecutor.getName().getvalue(), managedExecutor.getQualifiers());
        final String name = scopedResourceName(moduleId, managedExecutor.getName().getvalue());

        final Resource def = new Resource(name, jakarta.enterprise.concurrent.ManagedExecutorService.class.getName());

        def.setJndi(managedExecutor.getName().getvalue().replaceFirst("java:", ""));

        final Properties p = def.getProperties();

        final String contextName = resolveContextReference(moduleId, managedExecutor.getContextService());

        put(p, "Context", contextName);
        put(p, "HungTaskThreshold", managedExecutor.getHungTaskThreshold());
        put(p, "Max", managedExecutor.getMaxAsync());
        put(p, "Virtual", managedExecutor.isVirtual());
        put(p, "Qualifiers", Join.join(",", managedExecutor.getQualifiers()));

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
