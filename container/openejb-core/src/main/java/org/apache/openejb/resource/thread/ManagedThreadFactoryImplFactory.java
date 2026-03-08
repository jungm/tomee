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

package org.apache.openejb.resource.thread;

import org.apache.openejb.threads.impl.ContextServiceImplFactory;
import org.apache.openejb.threads.impl.ManagedThreadFactoryImpl;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import jakarta.enterprise.concurrent.ManagedThreadFactory;

public class ManagedThreadFactoryImplFactory {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, ManagedThreadFactoryImplFactory.class);

    private String prefix = "openejb-managed-thread-";
    private Integer priority;
    private String context;
    private boolean virtual;

    public ManagedThreadFactory create() {
        final boolean useVirtual = virtual && VirtualThreadSupport.isSupported();
        if (virtual && !useVirtual) {
            LOGGER.warning("ManagedThreadFactory configured with virtual=true but virtual threads are not supported by this JVM runtime. "
                    + "Falling back to platform threads.");
        }
        return new ManagedThreadFactoryImpl(prefix, priority, ContextServiceImplFactory.lookupOrDefault(context), useVirtual);
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
    }

    public void setContext(final String context) {
        this.context = context;
    }

    public void setVirtual(final boolean virtual) {
        this.virtual = virtual;
    }
}
