/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.mojarra;

import com.sun.faces.cdi.CdiExtension;
import org.apache.openejb.cdi.OptimizedLoaderService;
import org.apache.openejb.config.event.BeforeDeploymentEvent;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.spi.Service;
import org.apache.tomee.mojarra.owb.OwbCompatibleCdiExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TomEEMojarraService implements Service {
    @Override
    public void init(Properties props) throws Exception {
        SystemInstance.get().addObserver(this);
    }

    public void beforeDeployment(@Observes BeforeDeploymentEvent event) {
        Map<String, String> replacements = OptimizedLoaderService.EXTENSION_REPLACEMENTS.get();
        if (replacements == null) {
            replacements = new HashMap<>();
            OptimizedLoaderService.EXTENSION_REPLACEMENTS.set(replacements);
        }

        replacements.put(CdiExtension.class.getName(), OwbCompatibleCdiExtension.class.getName());
    }
}
