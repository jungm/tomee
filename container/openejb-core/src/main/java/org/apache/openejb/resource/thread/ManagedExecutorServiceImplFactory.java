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

import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.threads.impl.ContextServiceImpl;
import org.apache.openejb.threads.impl.ContextServiceImplFactory;
import org.apache.openejb.threads.impl.ManagedExecutorServiceImpl;
import org.apache.openejb.threads.impl.ManagedThreadFactoryImpl;
import org.apache.openejb.threads.reject.CURejectHandler;
import org.apache.openejb.util.Duration;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class ManagedExecutorServiceImplFactory {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, ManagedExecutorServiceImplFactory.class);

    private int core = 5;
    private int max = 25;
    private Duration keepAlive = new Duration("5 second");
    private int queue = 15;
    private String threadFactory;

    private String context;
    private boolean virtual;

    public static ManagedExecutorService lookupManagedExecutor(String name) throws NamingException {
        Object obj;
        try {
            obj = InitialContext.doLookup(name);
        } catch (NamingException e) {
            Context ctx = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();

            if (name.equals("java:comp/DefaultManagedExecutorService")) {
                name = "Default Managed Executor Service";
            }

            obj = ctx.lookup("openejb/Resource/" + name);
        }

        if (!(obj instanceof ManagedExecutorService mes)) {
            throw new IllegalArgumentException("Resource with id " + name
                    + " is not a ManagedExecutorService, but is " + obj.getClass().getName());
        }

        return mes;
    }

    public static ManagedExecutorServiceImpl lookup(final String name) throws NamingException {
        final ManagedExecutorService managedExecutorService = lookupManagedExecutor(name);
        if (!(managedExecutorService instanceof ManagedExecutorServiceImpl mes)) {
            throw new IllegalArgumentException("Resource with id " + name
                    + " is not a ManagedExecutorServiceImpl, but is " + managedExecutorService.getClass().getName());
        }
        return mes;
    }

    public ManagedExecutorServiceImpl create() {
        final ContextServiceImpl contextService = ContextServiceImplFactory.lookupOrDefault(context);
        return new ManagedExecutorServiceImpl(createExecutorService(contextService), contextService);
    }

    public ManagedExecutorServiceImpl create(final ContextServiceImpl contextService) {
        return new ManagedExecutorServiceImpl(createExecutorService(contextService), contextService);
    }

    private ExecutorService createExecutorService(final ContextServiceImpl contextService) {
        if (virtual && VirtualThreadSupport.isSupported()) {
            final ManagedThreadFactory managedThreadFactory = new ManagedThreadFactoryImpl(
                    ManagedThreadFactoryImpl.DEFAULT_PREFIX, null, contextService, true);
            return VirtualThreadSupport.newThreadPerTaskExecutor(managedThreadFactory);
        }
        if (virtual) {
            LOGGER.warning("ManagedExecutorService configured with virtual=true but virtual threads are not supported by this JVM runtime. "
                    + "Falling back to platform threads.");
        }

        final BlockingQueue<Runnable> blockingQueue;
        if (queue < 0) {
            blockingQueue = new LinkedBlockingQueue<>();
        } else if (queue == 0) {
            blockingQueue = new SynchronousQueue<>();
        } else {
            blockingQueue = new ArrayBlockingQueue<>(queue);
        }

        ManagedThreadFactory managedThreadFactory;
        try {
            managedThreadFactory = "org.apache.openejb.threads.impl.ManagedThreadFactoryImpl".equals(threadFactory) ?
                    new ManagedThreadFactoryImpl(ManagedThreadFactoryImpl.DEFAULT_PREFIX, null, contextService, false) :
                    ThreadFactories.findThreadFactory(threadFactory);
        } catch (final Exception e) {
            LOGGER.warning("Can't create configured thread factory: " + threadFactory, e);
            managedThreadFactory = new ManagedThreadFactoryImpl(ManagedThreadFactoryImpl.DEFAULT_PREFIX, null, contextService, false);
        }

        if (core > max) {
            LOGGER.warning("Core size (=" + core + ") is bigger than Max size (=" + max + "), lowering Core to Max");

            core = max;
        }

        return new ThreadPoolExecutor(core, max, keepAlive.getTime(), keepAlive.getUnit(), blockingQueue, managedThreadFactory, CURejectHandler.INSTANCE);
    }

    public void setCore(final int core) {
        this.core = core;
    }

    public void setMax(final int max) {
        this.max = max;
    }

    public void setKeepAlive(final Duration keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setQueue(final int queue) {
        this.queue = queue;
    }

    public void setThreadFactory(final String threadFactory) {
        this.threadFactory = threadFactory;
    }

    public String getContext() {
        return context;
    }

    public void setContext(final String context) {
        this.context = context;
    }

    public void setVirtual(final boolean virtual) {
        this.virtual = virtual;
    }
}
