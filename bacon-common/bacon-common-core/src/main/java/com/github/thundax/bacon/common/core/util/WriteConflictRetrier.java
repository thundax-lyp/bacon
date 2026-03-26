package com.github.thundax.bacon.common.core.util;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Generic exponential-backoff retry helper for write conflicts.
 */
public final class WriteConflictRetrier {

    private final int maxAttempts;
    private final long initialBackoffMillis;
    private final long maxBackoffMillis;
    private final Sleeper sleeper;

    public WriteConflictRetrier(int maxAttempts, long initialBackoffMillis, long maxBackoffMillis) {
        this(maxAttempts, initialBackoffMillis, maxBackoffMillis, Thread::sleep);
    }

    WriteConflictRetrier(int maxAttempts, long initialBackoffMillis, long maxBackoffMillis, Sleeper sleeper) {
        this.maxAttempts = Math.max(maxAttempts, 1);
        this.initialBackoffMillis = Math.max(initialBackoffMillis, 1L);
        this.maxBackoffMillis = Math.max(maxBackoffMillis, this.initialBackoffMillis);
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    public <T> T execute(Supplier<T> action, Predicate<RuntimeException> retryable, RetryListener retryListener) {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(retryable, "retryable");
        RetryListener listener = retryListener == null ? RetryListener.noop() : retryListener;

        long backoffMillis = initialBackoffMillis;
        int attempt = 0;
        while (attempt < maxAttempts) {
            attempt++;
            try {
                T result = action.get();
                if (attempt > 1) {
                    listener.onRecovered(attempt);
                }
                return result;
            } catch (RuntimeException ex) {
                if (!retryable.test(ex)) {
                    throw ex;
                }
                listener.onConflict(attempt, ex);
                if (attempt >= maxAttempts) {
                    listener.onExhausted(attempt, ex);
                    throw ex;
                }
                listener.onRetry(attempt, backoffMillis, ex);
                sleepBackoff(listener, backoffMillis, attempt, ex);
                backoffMillis = Math.min(backoffMillis * 2, maxBackoffMillis);
            }
        }
        throw new IllegalStateException("write-conflict-retry-exhausted");
    }

    private void sleepBackoff(RetryListener listener, long backoffMillis, int attempt, RuntimeException cause) {
        try {
            sleeper.sleep(backoffMillis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            listener.onInterrupted(attempt, backoffMillis, cause, interruptedException);
            throw new IllegalStateException("write-conflict-retry-interrupted", interruptedException);
        }
    }

    @FunctionalInterface
    interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    public interface RetryListener {

        static RetryListener noop() {
            return new RetryListener() {
            };
        }

        default void onConflict(int attempt, RuntimeException exception) {
        }

        default void onRetry(int attempt, long backoffMillis, RuntimeException exception) {
        }

        default void onRecovered(int attempt) {
        }

        default void onExhausted(int attempt, RuntimeException exception) {
        }

        default void onInterrupted(int attempt, long backoffMillis, RuntimeException cause,
                                   InterruptedException interruptedException) {
        }
    }
}
