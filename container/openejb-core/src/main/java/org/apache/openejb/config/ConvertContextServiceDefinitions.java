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
import org.apache.openejb.jee.ContextService;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.PropertyPlaceHolderHelper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class ConvertContextServiceDefinitions extends BaseConvertDefinitions {

    @Override
    public AppModule deploy(final AppModule appModule) throws OpenEJBException {
        final Map<String, ScopedDefinition<ContextService>> contextServices = new LinkedHashMap<>();
        final Map<String, ScopedDefinition<ContextService>> contextServicesFromCompManagedBeans = new LinkedHashMap<>();

        for (final ScopedJndiConsumer scopedConsumer : collectScopedConsumers(appModule)) {
            final String moduleId = scopedConsumer.moduleId();
            final org.apache.openejb.jee.JndiConsumer consumer = scopedConsumer.consumer();
            if (consumer == null) {
                continue;
            }
            final Map<String, ScopedDefinition<ContextService>> target = consumer instanceof CompManagedBean
                    ? contextServicesFromCompManagedBeans
                    : contextServices;

            for (final ContextService contextService : consumer.getContextServiceMap().values()) {
                /*
                 * TOMEE-2053: CompManagedBean may contain invalid context service definitions
                 * because it is never updated with content from ejb-jar.xml.
                 */
                target.put(scopedDefinitionKey(moduleId, contextService.getName().getvalue()),
                        new ScopedDefinition<>(moduleId, contextService));
            }
        }

        for (final Map.Entry<String, ScopedDefinition<ContextService>> entry : contextServicesFromCompManagedBeans.entrySet()) {
            // Interested only in ContextServices that come from non-JndiConsumers
            if (!contextServices.containsKey(entry.getKey())) {
                contextServices.put(entry.getKey(), entry.getValue());
            }
        }

        for (final ScopedDefinition<ContextService> definition : contextServices.values()) {
            appModule.getResources().add(toResource(definition.definition(), definition.moduleId()));
        }
        return appModule;
    }


    private Resource toResource(final ContextService contextService, final String moduleId) {
        validateQualifiedResourceName("ContextService", contextService.getName().getvalue(), contextService.getQualifier());
        final String name = scopedResourceName(moduleId, contextService.getName().getvalue());

        final Resource def = new Resource(name, jakarta.enterprise.concurrent.ContextService.class.getName());

        def.setJndi(contextService.getName().getvalue().replaceFirst("java:", ""));
        def.setType(jakarta.enterprise.concurrent.ContextService.class.getName());

        final Properties p = def.getProperties();
        put(p, "Propagated", Join.join(",", contextService.getPropagated()));
        put(p, "Cleared", Join.join(",", contextService.getCleared()));
        put(p, "Unchanged", Join.join(",", contextService.getUnchanged()));
        put(p, "Qualifiers", Join.join(",", contextService.getQualifier()));

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
