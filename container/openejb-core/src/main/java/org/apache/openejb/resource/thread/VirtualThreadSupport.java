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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

public final class VirtualThreadSupport {
    private static final Method THREAD_OF_VIRTUAL;
    private static final Method THREAD_BUILDER_NAME;
    private static final Method THREAD_BUILDER_UNSTARTED;
    private static final Method EXECUTORS_NEW_THREAD_PER_TASK_EXECUTOR;

    static {
        Method threadOfVirtual = null;
        Method threadBuilderName = null;
        Method threadBuilderUnstarted = null;
        Method executorsNewThreadPerTaskExecutor = null;
        try {
            threadOfVirtual = Thread.class.getMethod("ofVirtual");
            final Class<?> builderClass = threadOfVirtual.getReturnType();
            threadBuilderName = builderClass.getMethod("name", String.class);
            threadBuilderUnstarted = builderClass.getMethod("unstarted", Runnable.class);
            executorsNewThreadPerTaskExecutor = java.util.concurrent.Executors.class
                    .getMethod("newThreadPerTaskExecutor", ThreadFactory.class);
        } catch (final NoSuchMethodException ignored) {
            // virtual threads are not available on this runtime
        }
        THREAD_OF_VIRTUAL = threadOfVirtual;
        THREAD_BUILDER_NAME = threadBuilderName;
        THREAD_BUILDER_UNSTARTED = threadBuilderUnstarted;
        EXECUTORS_NEW_THREAD_PER_TASK_EXECUTOR = executorsNewThreadPerTaskExecutor;
    }

    private VirtualThreadSupport() {
        // no-op
    }

    public static boolean isSupported() {
        return THREAD_OF_VIRTUAL != null
                && THREAD_BUILDER_NAME != null
                && THREAD_BUILDER_UNSTARTED != null
                && EXECUTORS_NEW_THREAD_PER_TASK_EXECUTOR != null;
    }

    public static IllegalStateException unsupportedVirtualThreads(final String resourceType) {
        return new IllegalStateException(resourceType
                + " is configured with virtual=true, but virtual threads are not supported by this JVM runtime");
    }

    public static Thread newVirtualThread(final String name, final Runnable runnable) {
        if (!isSupported()) {
            throw unsupportedVirtualThreads("ManagedThreadFactory");
        }
        try {
            Object builder = THREAD_OF_VIRTUAL.invoke(null);
            builder = THREAD_BUILDER_NAME.invoke(builder, name);
            return Thread.class.cast(THREAD_BUILDER_UNSTARTED.invoke(builder, runnable));
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to create a virtual thread", unwrap(e));
        }
    }

    public static ExecutorService newThreadPerTaskExecutor(final ThreadFactory threadFactory) {
        if (!isSupported()) {
            throw unsupportedVirtualThreads("ManagedExecutorService");
        }
        try {
            return ExecutorService.class.cast(EXECUTORS_NEW_THREAD_PER_TASK_EXECUTOR.invoke(null, threadFactory));
        } catch (final IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to create a virtual-thread-per-task executor", unwrap(e));
        }
    }

    private static Throwable unwrap(final Exception e) {
        if (e instanceof InvocationTargetException && e.getCause() != null) {
            return e.getCause();
        }
        return e;
    }
}
