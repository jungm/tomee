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
import org.apache.openejb.jee.ManagedScheduledExecutor;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.PropertyPlaceHolderHelper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class ConvertManagedScheduledExecutorServiceDefinitions extends BaseConvertDefinitions {
    @Override
    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        final Map<String, ScopedDefinition<ManagedScheduledExecutor>> managedScheduledExecutors = new LinkedHashMap<>();
        final Map<String, ScopedDefinition<ManagedScheduledExecutor>> managedScheduledExecutorsFromCompManagedBeans = new LinkedHashMap<>();

        for (final ScopedJndiConsumer scopedConsumer : collectScopedConsumers(appModule)) {
            final String moduleId = scopedConsumer.moduleId();
            final org.apache.openejb.jee.JndiConsumer consumer = scopedConsumer.consumer();
            if (consumer == null) {
                continue;
            }
            final Map<String, ScopedDefinition<ManagedScheduledExecutor>> target = consumer instanceof CompManagedBean
                    ? managedScheduledExecutorsFromCompManagedBeans
                    : managedScheduledExecutors;
            for (final ManagedScheduledExecutor managedScheduledExecutor : consumer.getManagedScheduledExecutorMap().values()) {
                /*
                 * TOMEE-2053: CompManagedBean may contain invalid definitions
                 * because it is never updated with content from ejb-jar.xml.
                 */
                target.put(scopedDefinitionKey(moduleId, managedScheduledExecutor.getName().getvalue()),
                        new ScopedDefinition<>(moduleId, managedScheduledExecutor));
            }
        }

        for (final Map.Entry<String, ScopedDefinition<ManagedScheduledExecutor>> entry : managedScheduledExecutorsFromCompManagedBeans.entrySet()) {
            //Interested only in ManagedExecutorServices that come from non-JndiConsumers
            if (!managedScheduledExecutors.containsKey(entry.getKey())) {
                managedScheduledExecutors.put(entry.getKey(), entry.getValue());
            }
        }

        for (final ScopedDefinition<ManagedScheduledExecutor> managedScheduledExecutor : managedScheduledExecutors.values()) {
            appModule.getResources().add(toResource(managedScheduledExecutor.definition(), managedScheduledExecutor.moduleId()));
        }

        return appModule;
    }

    private Resource toResource(final ManagedScheduledExecutor managedScheduledExecutor, final String moduleId) {
        validateQualifiedResourceName("ManagedScheduledExecutorService", managedScheduledExecutor.getName().getvalue(), managedScheduledExecutor.getQualifiers());
        final String name = scopedResourceName(moduleId, managedScheduledExecutor.getName().getvalue());

        final Resource def = new Resource(name, jakarta.enterprise.concurrent.ManagedScheduledExecutorService.class.getName());

        def.setJndi(managedScheduledExecutor.getName().getvalue().replaceFirst("java:", ""));


        final String contextName = resolveContextReference(moduleId, managedScheduledExecutor.getContextService());

        final Properties p = def.getProperties();
        put(p, "Context", contextName);
        put(p, "HungTaskThreshold", managedScheduledExecutor.getHungTaskThreshold());
        put(p, "Core", managedScheduledExecutor.getMaxAsync());
        put(p, "Virtual", managedScheduledExecutor.isVirtual());
        put(p, "Qualifiers", Join.join(",", managedScheduledExecutor.getQualifiers()));

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
