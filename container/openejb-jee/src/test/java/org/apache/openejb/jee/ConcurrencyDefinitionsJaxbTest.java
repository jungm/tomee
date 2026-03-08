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
package org.apache.openejb.jee;

import junit.framework.TestCase;
import org.apache.openejb.jee.jba.JndiName;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class ConcurrencyDefinitionsJaxbTest extends TestCase {
    public void testWebAppConcurrencyDefinitionsRoundTrip() throws Exception {
        final WebApp webApp = new WebApp();

        final ContextService contextService = new ContextService();
        contextService.setName(jndiName("java:module/concurrent/MyContext"));
        contextService.getQualifier().add("com.example.MyQualifier");
        webApp.getContextServiceMap().put("java:module/concurrent/MyContext", contextService);

        final ManagedExecutor managedExecutor = new ManagedExecutor();
        managedExecutor.setName(jndiName("java:module/concurrent/MyExecutor"));
        managedExecutor.setContextService(jndiName("java:comp/DefaultContextService"));
        managedExecutor.setVirtual(Boolean.TRUE);
        managedExecutor.getQualifiers().add("com.example.ExecutorQualifier");
        webApp.getManagedExecutorMap().put("java:module/concurrent/MyExecutor", managedExecutor);

        final ManagedScheduledExecutor managedScheduledExecutor = new ManagedScheduledExecutor();
        managedScheduledExecutor.setName(jndiName("java:module/concurrent/MyScheduledExecutor"));
        managedScheduledExecutor.setContextService(jndiName("java:comp/DefaultContextService"));
        managedScheduledExecutor.setVirtual(Boolean.TRUE);
        managedScheduledExecutor.getQualifiers().add("com.example.ScheduledQualifier");
        webApp.getManagedScheduledExecutorMap().put("java:module/concurrent/MyScheduledExecutor", managedScheduledExecutor);

        final ManagedThreadFactory managedThreadFactory = new ManagedThreadFactory();
        managedThreadFactory.setName(jndiName("java:module/concurrent/MyThreadFactory"));
        managedThreadFactory.setContextService(jndiName("java:comp/DefaultContextService"));
        managedThreadFactory.setVirtual(Boolean.TRUE);
        managedThreadFactory.getQualifiers().add("com.example.ThreadFactoryQualifier");
        webApp.getManagedThreadFactoryMap().put("java:module/concurrent/MyThreadFactory", managedThreadFactory);

        final String xml = JaxbJavaee.marshal(WebApp.class, webApp);

        final WebApp unmarshalled = WebApp.class.cast(JaxbJavaee.unmarshalJavaee(WebApp.class,
            new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));

        final ContextService unmarshalledContextService = unmarshalled.getContextServiceMap().get("java:module/concurrent/MyContext");
        assertNotNull(unmarshalledContextService);
        assertEquals(1, unmarshalledContextService.getQualifier().size());
        assertEquals("com.example.MyQualifier", unmarshalledContextService.getQualifier().get(0));

        final ManagedExecutor unmarshalledManagedExecutor = unmarshalled.getManagedExecutorMap().get("java:module/concurrent/MyExecutor");
        assertNotNull(unmarshalledManagedExecutor);
        assertEquals(Boolean.TRUE, unmarshalledManagedExecutor.isVirtual());
        assertEquals(1, unmarshalledManagedExecutor.getQualifiers().size());
        assertEquals("com.example.ExecutorQualifier", unmarshalledManagedExecutor.getQualifiers().get(0));

        final ManagedScheduledExecutor unmarshalledManagedScheduledExecutor = unmarshalled.getManagedScheduledExecutorMap().get("java:module/concurrent/MyScheduledExecutor");
        assertNotNull(unmarshalledManagedScheduledExecutor);
        assertEquals(Boolean.TRUE, unmarshalledManagedScheduledExecutor.isVirtual());
        assertEquals(1, unmarshalledManagedScheduledExecutor.getQualifiers().size());
        assertEquals("com.example.ScheduledQualifier", unmarshalledManagedScheduledExecutor.getQualifiers().get(0));

        final ManagedThreadFactory unmarshalledManagedThreadFactory = unmarshalled.getManagedThreadFactoryMap().get("java:module/concurrent/MyThreadFactory");
        assertNotNull(unmarshalledManagedThreadFactory);
        assertEquals(Boolean.TRUE, unmarshalledManagedThreadFactory.isVirtual());
        assertEquals(1, unmarshalledManagedThreadFactory.getQualifiers().size());
        assertEquals("com.example.ThreadFactoryQualifier", unmarshalledManagedThreadFactory.getQualifiers().get(0));
    }

    private JndiName jndiName(final String value) {
        final JndiName jndiName = new JndiName();
        jndiName.setvalue(value);
        return jndiName;
    }
}
