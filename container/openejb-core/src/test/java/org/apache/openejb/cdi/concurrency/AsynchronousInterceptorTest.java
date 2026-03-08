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
package org.apache.openejb.cdi.concurrency;

import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.concurrent.Trigger;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.Transactional;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AsynchronousInterceptorTest {
    @Test
    public void classLevelAsynchronousAnnotationIsAccepted() throws Exception {
        final Exception validation = validate(ClassLevelAsyncBean.class.getMethod("run"));
        Assert.assertNull(validation);
    }

    @Test
    public void transactionalRequiredIsRejected() throws Exception {
        final Exception validation = validate(TransactionalRequiredAsyncBean.class.getMethod("run"));
        Assert.assertTrue(validation instanceof UnsupportedOperationException);
    }

    @Test
    public void invalidRunAtWithoutSecondsIsRejected() throws Exception {
        final Exception validation = validate(InvalidRunAtAsyncBean.class.getMethod("run"));
        Assert.assertTrue(validation instanceof IllegalArgumentException);
    }

    @Test
    public void transactionalRequiresNewIsAccepted() throws Exception {
        final Exception validation = validate(TransactionalRequiresNewAsyncBean.class.getMethod("run"));
        Assert.assertNull(validation);
    }

    @Test
    public void scheduledVoidInvocationDoesNotCompleteFuture() throws Exception {
        final AsynchronousInterceptor interceptor = new AsynchronousInterceptor();
        final CompletableFuture<Object> future = new CompletableFuture<>();
        final InvocationContext invocationContext = new SimpleInvocationContext(
                VoidScheduledAsyncBean.class.getMethod("run"), () -> null);
        final Trigger trigger = triggerFor(VoidScheduledAsyncBean.class.getMethod("run"), future);

        final Method invokeScheduledExecution = AsynchronousInterceptor.class
                .getDeclaredMethod("invokeScheduledExecution", InvocationContext.class, CompletableFuture.class, Class
                        .forName("org.apache.openejb.cdi.concurrency.AsynchronousInterceptor$ScheduledAsynchronousTrigger"));
        invokeScheduledExecution.setAccessible(true);
        invokeScheduledExecution.invoke(interceptor, invocationContext, future, trigger);

        Assert.assertFalse(future.isDone());
    }

    @Test
    public void scheduledCompletionStageStopsScheduleOnNonNullReturn() throws Exception {
        final AsynchronousInterceptor interceptor = new AsynchronousInterceptor();
        final CompletableFuture<Object> future = new CompletableFuture<>();
        final InvocationContext invocationContext = new SimpleInvocationContext(
                CompletionStageScheduledAsyncBean.class.getMethod("run"), () -> future);
        final Trigger trigger = triggerFor(CompletionStageScheduledAsyncBean.class.getMethod("run"), future);

        final Method invokeScheduledExecution = AsynchronousInterceptor.class
                .getDeclaredMethod("invokeScheduledExecution", InvocationContext.class, CompletableFuture.class, Class
                        .forName("org.apache.openejb.cdi.concurrency.AsynchronousInterceptor$ScheduledAsynchronousTrigger"));
        invokeScheduledExecution.setAccessible(true);
        invokeScheduledExecution.invoke(interceptor, invocationContext, future, trigger);

        Assert.assertFalse(future.isDone());
        Assert.assertNull(trigger.getNextRunTime(null, new Date()));
    }

    @Test
    public void scheduledWrappedExceptionMessageIsPreserved() throws Exception {
        final AsynchronousInterceptor interceptor = new AsynchronousInterceptor();
        final CompletableFuture<Object> future = new CompletableFuture<>();
        final String expectedMessage = "scheduled-error";
        final InvocationContext invocationContext = new SimpleInvocationContext(
                CompletionStageScheduledAsyncBean.class.getMethod("run"),
                () -> {
                    throw new InvocationTargetException(new RuntimeException(expectedMessage));
                });
        final Trigger trigger = triggerFor(CompletionStageScheduledAsyncBean.class.getMethod("run"), future);

        final Method invokeScheduledExecution = AsynchronousInterceptor.class
                .getDeclaredMethod("invokeScheduledExecution", InvocationContext.class, CompletableFuture.class, Class
                        .forName("org.apache.openejb.cdi.concurrency.AsynchronousInterceptor$ScheduledAsynchronousTrigger"));
        invokeScheduledExecution.setAccessible(true);
        invokeScheduledExecution.invoke(interceptor, invocationContext, future, trigger);

        Assert.assertTrue(future.isCompletedExceptionally());
        try {
            future.get();
            Assert.fail("Expected future to complete exceptionally");
        } catch (final ExecutionException e) {
            Assert.assertTrue(e.getMessage().contains(expectedMessage));
        }
    }

    @Test
    public void singleWrappedExceptionMessageIsPreserved() throws Exception {
        final AsynchronousInterceptor interceptor = new AsynchronousInterceptor();
        final CompletableFuture<Object> future = new CompletableFuture<>();
        final String expectedMessage = "single-error";
        final InvocationContext invocationContext = new SimpleInvocationContext(
                ClassLevelAsyncBean.class.getMethod("run"),
                () -> {
                    throw new InvocationTargetException(new RuntimeException(expectedMessage));
                });

        final Method invokeSingleExecution = AsynchronousInterceptor.class
                .getDeclaredMethod("invokeSingleExecution", InvocationContext.class, CompletableFuture.class);
        invokeSingleExecution.setAccessible(true);
        invokeSingleExecution.invoke(interceptor, invocationContext, future);

        Assert.assertTrue(future.isCompletedExceptionally());
        try {
            future.get();
            Assert.fail("Expected future to complete exceptionally");
        } catch (final ExecutionException e) {
            Assert.assertTrue(e.getMessage().contains(expectedMessage));
        }
    }

    private Exception validate(final Method method) throws Exception {
        final AsynchronousInterceptor interceptor = new AsynchronousInterceptor();
        final Method validateMethod = AsynchronousInterceptor.class.getDeclaredMethod("validate", Method.class);
        validateMethod.setAccessible(true);
        return Exception.class.cast(validateMethod.invoke(interceptor, method));
    }

    @Asynchronous
    public static class ClassLevelAsyncBean {
        public CompletableFuture<String> run() {
            return null;
        }
    }

    @Asynchronous
    @Transactional(Transactional.TxType.REQUIRED)
    public static class TransactionalRequiredAsyncBean {
        public CompletableFuture<String> run() {
            return null;
        }
    }

    public static class InvalidRunAtAsyncBean {
        @Asynchronous(runAt = @Schedule(seconds = {}))
        public CompletableFuture<String> run() {
            return null;
        }
    }

    @Asynchronous
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public static class TransactionalRequiresNewAsyncBean {
        public CompletableFuture<String> run() {
            return null;
        }
    }

    public static class VoidScheduledAsyncBean {
        @Asynchronous(runAt = @Schedule(cron = "*/10 * * * * *"))
        public void run() {
            // no-op
        }
    }

    public static class CompletionStageScheduledAsyncBean {
        @Asynchronous(runAt = @Schedule(cron = "*/10 * * * * *"))
        public CompletableFuture<String> run() {
            return null;
        }
    }

    private Trigger triggerFor(final Method method, final CompletableFuture<Object> future) throws Exception {
        final Asynchronous asynchronous = method.getAnnotation(Asynchronous.class);
        final Class<?> triggerClass =
                Class.forName("org.apache.openejb.cdi.concurrency.AsynchronousInterceptor$ScheduledAsynchronousTrigger");
        final Constructor<?> constructor = triggerClass.getDeclaredConstructor(Schedule[].class, CompletableFuture.class);
        constructor.setAccessible(true);
        return Trigger.class.cast(constructor.newInstance((Object) asynchronous.runAt(), future));
    }

    private static class SimpleInvocationContext implements InvocationContext {
        private final Method method;
        private final ThrowingSupplier<Object> proceed;
        private final Map<String, Object> contextData = new HashMap<>();

        private SimpleInvocationContext(final Method method, final ThrowingSupplier<Object> proceed) {
            this.method = method;
            this.proceed = proceed;
        }

        @Override
        public Object getTarget() {
            return null;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Constructor<?> getConstructor() {
            return null;
        }

        @Override
        public Object[] getParameters() {
            return new Object[0];
        }

        @Override
        public void setParameters(final Object[] params) {
            // no-op
        }

        @Override
        public Map<String, Object> getContextData() {
            return contextData;
        }

        @Override
        public Object getTimer() {
            return null;
        }

        @Override
        public Object proceed() throws Exception {
            return proceed.get();
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
