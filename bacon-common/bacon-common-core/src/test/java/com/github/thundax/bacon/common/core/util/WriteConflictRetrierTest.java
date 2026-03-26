package com.github.thundax.bacon.common.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class WriteConflictRetrierTest {

    @Test
    void shouldRetryAndRecover() {
        AtomicInteger attempts = new AtomicInteger(0);
        List<Long> sleepValues = new ArrayList<>();
        WriteConflictRetrier retrier = new WriteConflictRetrier(5, 10L, 40L, sleepValues::add);

        String result = retrier.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new IllegalStateException("conflict");
            }
            return "ok";
        }, ex -> "conflict".equals(ex.getMessage()), null);

        assertEquals("ok", result);
        assertEquals(3, attempts.get());
        assertEquals(List.of(10L, 20L), sleepValues);
    }

    @Test
    void shouldThrowImmediatelyWhenNotRetryable() {
        WriteConflictRetrier retrier = new WriteConflictRetrier(3, 10L, 20L, millis -> {
        });

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                retrier.execute(() -> {
                    throw new IllegalArgumentException("bad");
                }, ex -> false, null));

        assertEquals("bad", exception.getMessage());
    }

    @Test
    void shouldThrowWhenRetryExhausted() {
        AtomicInteger attempts = new AtomicInteger(0);
        WriteConflictRetrier retrier = new WriteConflictRetrier(2, 10L, 20L, millis -> {
        });

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                retrier.execute(() -> {
                    attempts.incrementAndGet();
                    throw new IllegalStateException("conflict");
                }, ex -> true, null));

        assertEquals("conflict", exception.getMessage());
        assertEquals(2, attempts.get());
    }
}
