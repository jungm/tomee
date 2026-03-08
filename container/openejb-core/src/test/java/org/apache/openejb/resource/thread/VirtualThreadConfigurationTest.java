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

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.ri.sp.PseudoSecurityService;
import org.apache.openejb.spi.SecurityService;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

public class VirtualThreadConfigurationTest {
    @BeforeClass
    public static void forceSecurityService() {
        SystemInstance.get().setComponent(SecurityService.class, new PseudoSecurityService());
    }

    @Test
    public void virtualManagedExecutorFallsBackWhenUnsupported() throws Exception {
        Assume.assumeFalse(VirtualThreadSupport.isSupported());

        final ManagedExecutorServiceImplFactory factory = new ManagedExecutorServiceImplFactory();
        factory.setThreadFactory("org.apache.openejb.threads.impl.ManagedThreadFactoryImpl");
        factory.setVirtual(true);
        final jakarta.enterprise.concurrent.ManagedExecutorService executor = factory.create();
        Assert.assertNotNull(executor);
        Assert.assertTrue(executor.submit(() -> Thread.currentThread() != null).get());
    }

    @Test
    public void virtualManagedThreadFactoryFallsBackWhenUnsupported() {
        Assume.assumeFalse(VirtualThreadSupport.isSupported());

        final ManagedThreadFactoryImplFactory factory = new ManagedThreadFactoryImplFactory();
        factory.setVirtual(true);
        final jakarta.enterprise.concurrent.ManagedThreadFactory managedThreadFactory = factory.create();
        Assert.assertNotNull(managedThreadFactory);
        Assert.assertNotNull(managedThreadFactory.newThread(() -> {
            // no-op
        }));
    }
}
