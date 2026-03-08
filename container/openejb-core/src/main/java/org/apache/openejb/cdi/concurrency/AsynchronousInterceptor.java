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

import jakarta.annotation.Priority;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.CronTrigger;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.concurrent.Trigger;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import org.apache.openejb.resource.thread.ManagedExecutorServiceImplFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Interceptor
@Asynchronous
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 5)
public class AsynchronousInterceptor {
    public static final String MP_ASYNC_ANNOTATION_NAME = "org.eclipse.microprofile.faulttolerance.Asynchronous";
    private static final String DEFAULT_MANAGED_SCHEDULED_EXECUTOR = "java:comp/DefaultManagedScheduledExecutorService";

    // ensure validation logic required by the spec only runs once per invoked Method
    private final Map<Method, Exception> validationCache = new ConcurrentHashMap<>();

    @AroundInvoke
    public Object aroundInvoke(final InvocationContext ctx) throws Exception {
        Exception exception = validationCache.computeIfAbsent(ctx.getMethod(), this::validate);
        if (exception != null) {
            throw exception;
        }

        final Asynchronous asynchronous = resolveAsynchronous(ctx.getMethod());
        if (asynchronous == null) {
            throw new UnsupportedOperationException("Asynchronous annotation must be placed on a method or class");
        }

        ManagedExecutorService mes;
        try {
            mes = ManagedExecutorServiceImplFactory.lookupManagedExecutor(asynchronous.executor());
        } catch (NamingException | IllegalArgumentException e) {
            throw new RejectedExecutionException("Cannot lookup ManagedExecutorService", e);
        }

        final CompletableFuture<Object> future = mes.newIncompleteFuture();
        if (asynchronous.runAt().length == 0) {
            mes.execute(() -> invokeSingleExecution(ctx, future));
        } else {
            final ManagedScheduledExecutorService scheduler = resolveScheduler();
            final ScheduledAsynchronousTrigger trigger = new ScheduledAsynchronousTrigger(asynchronous.runAt(), future);
            final ScheduledFuture<?> scheduledFuture = scheduler.schedule(
                    contextualScheduledExecution(ctx, future, mes, trigger), trigger);
            future.whenComplete((value, error) -> scheduledFuture.cancel(true));
        }

        return ctx.getMethod().getReturnType() == Void.TYPE ? null : future;
    }

    private Callable<Object> contextualScheduledExecution(final InvocationContext ctx,
                                                          final CompletableFuture<Object> future,
                                                          final ManagedExecutorService executor,
                                                          final ScheduledAsynchronousTrigger trigger) {
        final Callable<Object> contextual = executor.getContextService()
                .contextualCallable(() -> invokeScheduledExecution(ctx, future, trigger));

        return () -> {
            if (future.isDone()) {
                return null;
            }
            try {
                return contextual.call();
            } catch (final Exception e) {
                trigger.stop();
                future.completeExceptionally(unwrapThrowable(e));
                return null;
            }
        };
    }

    private ManagedScheduledExecutorService resolveScheduler() {
        final Object scheduledExecutor;
        try {
            scheduledExecutor = InitialContext.doLookup(DEFAULT_MANAGED_SCHEDULED_EXECUTOR);
        } catch (final NamingException e) {
            throw new RejectedExecutionException("Cannot lookup ManagedScheduledExecutorService", e);
        }

        if (!(scheduledExecutor instanceof ManagedScheduledExecutorService mses)) {
            throw new RejectedExecutionException("Resource " + DEFAULT_MANAGED_SCHEDULED_EXECUTOR
                    + " is not a ManagedScheduledExecutorService");
        }
        return mses;
    }

    private void invokeSingleExecution(final InvocationContext ctx, final CompletableFuture<Object> future) {
        try {
            final CompletionStage<?> result = proceed(ctx, future);
            completeFromResult(future, result);
        } catch (final Exception e) {
            future.completeExceptionally(unwrapThrowable(e));
        }
    }

    private Object invokeScheduledExecution(final InvocationContext ctx,
                                            final CompletableFuture<Object> future,
                                            final ScheduledAsynchronousTrigger trigger) throws Exception {
        if (future.isDone()) {
            trigger.stop();
            return null;
        }

        final CompletionStage<?> result;
        try {
            result = proceed(ctx, future);
        } catch (final Exception e) {
            trigger.stop();
            future.completeExceptionally(unwrapThrowable(e));
            return null;
        }
        if (result == null) {
            return null;
        }

        trigger.stop();
        if (result != future) {
            result.whenComplete((resultInternal, err) -> {
                if (err != null) {
                    future.completeExceptionally(unwrapThrowable(err));
                } else {
                    future.complete(resultInternal);
                }
            });
        }
        return result;
    }

    private CompletionStage<?> proceed(final InvocationContext ctx, final CompletableFuture<Object> future) throws Exception {
        Asynchronous.Result.setFuture(future);
        try {
            return (CompletionStage<?>) ctx.proceed();
        } finally {
            Asynchronous.Result.setFuture(null);
        }
    }

    private void completeFromResult(final CompletableFuture<Object> future, final CompletionStage<?> result) {
        if (result == null) {
            future.complete(null);
            return;
        }
        if (result == future) {
            return;
        }

        result.whenComplete((resultInternal, err) -> {
            if (err != null) {
                future.completeExceptionally(unwrapThrowable(err));
            } else {
                future.complete(resultInternal);
            }
        });
    }

    private Throwable unwrapThrowable(final Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            final Throwable next;
            if (current instanceof InvocationTargetException ite && ite.getCause() != null) {
                next = ite.getCause();
            } else if (current instanceof UndeclaredThrowableException ute && ute.getUndeclaredThrowable() != null) {
                next = ute.getUndeclaredThrowable();
            } else if ((current instanceof CompletionException || current instanceof ExecutionException) && current.getCause() != null) {
                next = current.getCause();
            } else {
                return current;
            }

            if (next == current) {
                return current;
            }
            current = next;
        }
        return throwable;
    }

    private Exception validate(final Method method) {
        if (hasMpAsyncAnnotation(method.getAnnotations()) || hasMpAsyncAnnotation(method.getDeclaringClass().getAnnotations())) {
            return new UnsupportedOperationException("Combining " + Asynchronous.class.getName()
                    + " and " + MP_ASYNC_ANNOTATION_NAME + " on the same method/class is not supported");
        }

        final Asynchronous asynchronous = resolveAsynchronous(method);
        if (asynchronous == null) {
            return new UnsupportedOperationException("Asynchronous annotation must be placed on a method or class");
        }

        Class<?> returnType = method.getReturnType();
        if (returnType != Void.TYPE && returnType != CompletableFuture.class && returnType != CompletionStage.class) {
            return new UnsupportedOperationException("Asynchronous annotation must be placed on a method that returns either void, CompletableFuture or CompletionStage");
        }

        final Transactional transactional = resolveTransactional(method);
        if (transactional != null && transactional.value() != TxType.REQUIRES_NEW && transactional.value() != TxType.NOT_SUPPORTED) {
            return new UnsupportedOperationException("@Transactional on @Asynchronous methods only supports TxType.REQUIRES_NEW or TxType.NOT_SUPPORTED");
        }

        for (final Schedule schedule : asynchronous.runAt()) {
            if (schedule.skipIfLateBy() <= 0) {
                return new IllegalArgumentException("Asynchronous runAt skipIfLateBy must be greater than 0");
            }
            if (schedule.cron().isEmpty() && schedule.seconds().length == 0) {
                return new IllegalArgumentException("Asynchronous runAt seconds must not be empty when cron is not specified");
            }
        }

        return null;
    }

    private Asynchronous resolveAsynchronous(final Method method) {
        final Asynchronous methodLevel = method.getAnnotation(Asynchronous.class);
        if (methodLevel != null) {
            return methodLevel;
        }
        return method.getDeclaringClass().getAnnotation(Asynchronous.class);
    }

    private Transactional resolveTransactional(final Method method) {
        final Transactional methodLevel = method.getAnnotation(Transactional.class);
        if (methodLevel != null) {
            return methodLevel;
        }
        return method.getDeclaringClass().getAnnotation(Transactional.class);
    }

    private boolean hasMpAsyncAnnotation(Annotation[] declaredAnnotations) {
        return Arrays.stream(declaredAnnotations)
                .map(it -> it.annotationType().getName())
                .anyMatch(it -> it.equals(MP_ASYNC_ANNOTATION_NAME));
    }

    private static final class ScheduledAsynchronousTrigger implements Trigger {
        private final Entry[] entries;
        private final CompletableFuture<?> completion;
        private volatile boolean stopRequested;

        private volatile Date lastNextRunTime;
        private volatile long lastSkipIfLateBySeconds;

        private ScheduledAsynchronousTrigger(final Schedule[] schedules, final CompletableFuture<?> completion) {
            this.completion = completion;
            this.entries = Arrays.stream(schedules).map(Entry::new).toArray(Entry[]::new);
        }

        @Override
        public Date getNextRunTime(final jakarta.enterprise.concurrent.LastExecution lastExecutionInfo, final Date taskScheduledTime) {
            if (completion.isDone() || stopRequested) {
                return null;
            }

            Date nextRun = null;
            long skipIfLateBy = 600L;
            for (final Entry entry : entries) {
                final Date candidate = entry.trigger.getNextRunTime(lastExecutionInfo, taskScheduledTime);
                if (candidate == null) {
                    continue;
                }
                if (nextRun == null || candidate.before(nextRun)) {
                    nextRun = candidate;
                    skipIfLateBy = entry.skipIfLateBy;
                }
            }

            if (nextRun != null) {
                this.lastNextRunTime = nextRun;
                this.lastSkipIfLateBySeconds = skipIfLateBy;
            }
            return nextRun;
        }

        @Override
        public boolean skipRun(final jakarta.enterprise.concurrent.LastExecution lastExecutionInfo, final Date scheduledRunTime) {
            if (lastNextRunTime == null) {
                return false;
            }
            return System.currentTimeMillis() - lastNextRunTime.getTime()
                    > TimeUnit.SECONDS.toMillis(lastSkipIfLateBySeconds);
        }

        private void stop() {
            this.stopRequested = true;
        }

        private static final class Entry {
            private final CronTrigger trigger;
            private final long skipIfLateBy;

            private Entry(final Schedule schedule) {
                this.skipIfLateBy = schedule.skipIfLateBy();
                final ZoneId zoneId = schedule.zone().isEmpty() ? ZoneId.systemDefault() : ZoneId.of(schedule.zone());
                if (!schedule.cron().isEmpty()) {
                    this.trigger = new CronTrigger(schedule.cron(), zoneId);
                } else {
                    this.trigger = new CronTrigger(zoneId)
                            .months(schedule.months())
                            .daysOfMonth(schedule.daysOfMonth())
                            .daysOfWeek(schedule.daysOfWeek())
                            .hours(schedule.hours())
                            .minutes(schedule.minutes())
                            .seconds(schedule.seconds());
                }
            }
        }
    }
}
