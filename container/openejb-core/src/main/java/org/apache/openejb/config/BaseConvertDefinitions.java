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

import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.jba.JndiName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseConvertDefinitions implements DynamicDeployer {
    protected static final String DEFAULT_CONTEXT_SERVICE_JNDI = "java:comp/DefaultContextService";

    protected record ScopedJndiConsumer(String moduleId, JndiConsumer consumer) {
    }

    protected String cleanUpName(final String factory) {
        String name = factory;
        name = name.replaceFirst("java:comp/env/", "");
        name = name.replaceFirst("java:/", "");
        name = name.replaceFirst("java:", "");
        return name;
    }

    protected boolean isModuleScopedName(final String jndiName) {
        return jndiName != null
                && (jndiName.startsWith("java:module/") || jndiName.startsWith("module/"));
    }

    protected String scopedDefinitionKey(final String moduleId, final String jndiName) {
        if (isModuleScopedName(jndiName)) {
            return (moduleId == null ? "" : moduleId) + "|" + jndiName;
        }
        return jndiName;
    }

    protected String scopedResourceName(final String moduleId, final String jndiName) {
        final String cleaned = cleanUpName(jndiName);
        if (!isModuleScopedName(jndiName) || moduleId == null || moduleId.isEmpty()) {
            return cleaned;
        }
        return moduleId + "/" + cleaned;
    }

    protected String scopedContextReference(final String moduleId, final String contextName) {
        if (contextName == null) {
            return null;
        }
        if (DEFAULT_CONTEXT_SERVICE_JNDI.equals(contextName)) {
            return "Default Context Service";
        }
        return scopedResourceName(moduleId, contextName);
    }

    protected String resolveContextReference(final String moduleId, final JndiName contextService) {
        if (contextService == null || contextService.getvalue() == null || contextService.getvalue().isBlank()) {
            return scopedContextReference(moduleId, DEFAULT_CONTEXT_SERVICE_JNDI);
        }
        return scopedContextReference(moduleId, contextService.getvalue());
    }

    protected List<JndiConsumer> collectConsumers(final AppModule appModule) {

        final List<JndiConsumer> jndiConsumers = new ArrayList<>();

        for (final ClientModule module : appModule.getClientModules()) {
            final JndiConsumer consumer = module.getApplicationClient();
            if (consumer == null) {
                continue;
            }
            jndiConsumers.add(consumer);
        }

        for (final WebModule webModule : appModule.getWebModules()) {
            final JndiConsumer consumer = webModule.getWebApp();
            if (consumer == null) {
                continue;
            }
            jndiConsumers.add(consumer);
        }

        for (final EjbModule ejbModule : appModule.getEjbModules()) {
            Collections.addAll(jndiConsumers, ejbModule.getEjbJar().getEnterpriseBeans());
        }

        if (appModule.getApplication() != null) {
            jndiConsumers.add(appModule.getApplication());
        }

        return jndiConsumers;
    }

    protected List<ScopedJndiConsumer> collectScopedConsumers(final AppModule appModule) {
        final List<ScopedJndiConsumer> jndiConsumers = new ArrayList<>();

        for (final ClientModule module : appModule.getClientModules()) {
            final JndiConsumer consumer = module.getApplicationClient();
            if (consumer == null) {
                continue;
            }
            jndiConsumers.add(new ScopedJndiConsumer(module.getModuleId(), consumer));
        }

        for (final WebModule module : appModule.getWebModules()) {
            final JndiConsumer consumer = module.getWebApp();
            if (consumer == null) {
                continue;
            }
            jndiConsumers.add(new ScopedJndiConsumer(module.getModuleId(), consumer));
        }

        for (final EjbModule module : appModule.getEjbModules()) {
            for (final JndiConsumer consumer : module.getEjbJar().getEnterpriseBeans()) {
                jndiConsumers.add(new ScopedJndiConsumer(module.getModuleId(), consumer));
            }
        }

        if (appModule.getApplication() != null) {
            jndiConsumers.add(new ScopedJndiConsumer(appModule.getModuleId(), appModule.getApplication()));
        }

        return jndiConsumers;
    }

    protected void validateQualifiedResourceName(final String resourceType,
                                                 final String jndiName,
                                                 final List<String> qualifiers) {
        if (qualifiers == null || qualifiers.isEmpty()) {
            return;
        }
        if (jndiName != null && jndiName.startsWith("java:global/")) {
            throw new IllegalArgumentException(resourceType + " with qualifiers must not use a java:global name: " + jndiName);
        }
    }

}
