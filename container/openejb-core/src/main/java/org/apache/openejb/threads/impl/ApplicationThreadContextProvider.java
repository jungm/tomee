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
package org.apache.openejb.threads.impl;

import jakarta.enterprise.concurrent.ContextServiceDefinition;
import jakarta.enterprise.concurrent.spi.ThreadContextProvider;
import jakarta.enterprise.concurrent.spi.ThreadContextRestorer;
import jakarta.enterprise.concurrent.spi.ThreadContextSnapshot;
import org.apache.openejb.AppContext;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.AppFinder;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;

public class ApplicationThreadContextProvider implements ThreadContextProvider, Serializable {
    public static final ApplicationThreadContextProvider INSTANCE = new ApplicationThreadContextProvider();
    private static final ThreadLocal<ThreadContext> RAW_THREAD_CONTEXT_STORAGE = initRawThreadContextStorage();

    @Override
    public ThreadContextSnapshot currentContext(final Map<String, String> props) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final ThreadContext threadContext = ThreadContext.getThreadContext();
        final AppContext appContext = AppFinder.findAppContextOrWeb(contextClassLoader, AppFinder.AppContextTransformer.INSTANCE);
        final Object appId = appContext != null ? appContext.getId() : findAppId(threadContext);

        return new ApplicationThreadContextSnapshot(
                appId,
                appContext != null ? findWebContextId(appContext, contextClassLoader) : null,
                contextClassLoader,
                threadContext);
    }

    @Override
    public ThreadContextSnapshot clearedContext(final Map<String, String> props) {
        return ThreadContextProviderUtil.NOOP_SNAPSHOT;
    }

    @Override
    public String getThreadContextType() {
        return ContextServiceDefinition.APPLICATION;
    }

    private String findWebContextId(final AppContext appContext, final ClassLoader classLoader) {
        for (final WebContext webContext : appContext.getWebContexts()) {
            final ClassLoader webClassLoader = webContext.getClassLoader();
            if (webClassLoader == classLoader
                    || (webClassLoader != null && webClassLoader.equals(classLoader))
                    || (classLoader != null && classLoader.equals(webClassLoader))) {
                return webContext.getId();
            }
        }
        return null;
    }

    private Object findAppId(final ThreadContext threadContext) {
        if (threadContext == null || threadContext.getBeanContext() == null
                || threadContext.getBeanContext().getModuleContext() == null
                || threadContext.getBeanContext().getModuleContext().getAppContext() == null) {
            return null;
        }
        return threadContext.getBeanContext().getModuleContext().getAppContext().getId();
    }

    @SuppressWarnings("unchecked")
    private static ThreadLocal<ThreadContext> initRawThreadContextStorage() {
        try {
            final Field field = ThreadContext.class.getDeclaredField("threadStorage");
            field.setAccessible(true);
            return ThreadLocal.class.cast(field.get(null));
        } catch (final Exception e) {
            return null;
        }
    }

    private static void rawSetThreadContext(final ThreadContext threadContext) {
        if (RAW_THREAD_CONTEXT_STORAGE == null) {
            return;
        }
        if (threadContext == null) {
            RAW_THREAD_CONTEXT_STORAGE.remove();
        } else {
            RAW_THREAD_CONTEXT_STORAGE.set(threadContext);
        }
    }

    public static class ApplicationThreadContextSnapshot implements ThreadContextSnapshot, Serializable {
        private final Object appId;
        private final String webContextId;
        private final transient ClassLoader capturedClassLoader;
        private final ThreadContext threadContext;

        public ApplicationThreadContextSnapshot(final Object appId,
                                                final String webContextId,
                                                final ClassLoader capturedClassLoader,
                                                final ThreadContext threadContext) {
            this.appId = appId;
            this.webContextId = webContextId;
            this.capturedClassLoader = capturedClassLoader;
            this.threadContext = threadContext;
        }

        @Override
        public ThreadContextRestorer begin() {
            if (appId == null && capturedClassLoader == null && threadContext == null) {
                return ThreadContextProviderUtil.NOOP_RESTORER;
            }

            final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            AppContext appContext = null;
            if (containerSystem != null && appId != null) {
                appContext = containerSystem.getAppContext(appId);
            }

            final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();

            // Don't touch ThreadContext if it is already correct or none was captured
            final ThreadContext currentThreadContext = ThreadContext.getThreadContext();
            final boolean changeThreadContext = threadContext != null && threadContext != currentThreadContext;
            final boolean hideExecutionThreadContext = threadContext == null && currentThreadContext != null;
            final ThreadContext oldThreadContext;
            if (changeThreadContext) {
                oldThreadContext = ThreadContext.enter(new ThreadContext(threadContext));
            } else if (hideExecutionThreadContext) {
                oldThreadContext = currentThreadContext;
                rawSetThreadContext(null);
            } else {
                oldThreadContext = null;
            }

            // ThreadContext.enter() can also modify the TCCL, so set the captured component classloader afterwards.
            final ClassLoader newCl = resolveClassLoader(containerSystem, appContext);
            if (newCl != null) {
                Thread.currentThread().setContextClassLoader(newCl);
            }
            return new ApplicationThreadContextRestorer(oldCl, oldThreadContext, changeThreadContext, hideExecutionThreadContext);
        }

        private ClassLoader resolveClassLoader(final ContainerSystem containerSystem, final AppContext appContext) {
            if (capturedClassLoader != null) {
                return capturedClassLoader;
            }

            if (containerSystem != null && webContextId != null) {
                final WebContext webContext = containerSystem.getWebContext(webContextId);
                if (webContext != null && webContext.getClassLoader() != null) {
                    return webContext.getClassLoader();
                }
            }

            if (appContext != null) {
                return appContext.getClassLoader();
            }

            if (threadContext != null && threadContext.getBeanContext() != null) {
                return threadContext.getBeanContext().getClassLoader();
            }

            return null;
        }

        @Override
        public String toString() {
            return "ApplicationThreadContextSnapshot@" + System.identityHashCode(this) +
                    "{appId=" + appId +
                    "{webContextId=" + webContextId +
                    "{capturedClassLoader=" + capturedClassLoader +
                    "{threadContext=" + threadContext +
                    '}';
        }

    }

    public static class ApplicationThreadContextRestorer implements ThreadContextRestorer {
        private final ClassLoader oldClassLoader;
        private final ThreadContext oldThreadContext;
        private final boolean exitThreadContext;
        private final boolean restoreHiddenThreadContext;

        public ApplicationThreadContextRestorer(final ClassLoader oldClassLoader, final ThreadContext oldThreadContext, boolean exitThreadContext) {
            this(oldClassLoader, oldThreadContext, exitThreadContext, false);
        }

        public ApplicationThreadContextRestorer(final ClassLoader oldClassLoader,
                                                final ThreadContext oldThreadContext,
                                                final boolean exitThreadContext,
                                                final boolean restoreHiddenThreadContext) {
            this.oldClassLoader = oldClassLoader;
            this.oldThreadContext = oldThreadContext;
            this.exitThreadContext = exitThreadContext;
            this.restoreHiddenThreadContext = restoreHiddenThreadContext;
        }

        @Override
        public void endContext() throws IllegalStateException {
            if (oldClassLoader != null) {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }

            if (exitThreadContext) {
                ThreadContext.exit(oldThreadContext);
            } else if (restoreHiddenThreadContext) {
                rawSetThreadContext(oldThreadContext);
            }
        }

        @Override
        public String toString() {
            return "ApplicationThreadContextRestorer@" + System.identityHashCode(this) +
                    "{oldClassLoader=" + oldClassLoader +
                    "{oldThreadContext=" + oldThreadContext +
                    "{exitThreadContext=" + exitThreadContext +
                    "{restoreHiddenThreadContext=" + restoreHiddenThreadContext +
                    '}';
        }

    }
}
