package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import io.micrometer.core.instrument.Metrics;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryWriteRetrier {

    private final int maxAttempts;
    private final long initialBackoffMillis;
    private final long maxBackoffMillis;

    public InventoryWriteRetrier() {
        this(6, 20L, 500L);
    }

    @Autowired
    public InventoryWriteRetrier(
            @Value("${bacon.inventory.retry.max-attempts:3}") int maxAttempts,
            @Value("${bacon.inventory.retry.initial-backoff-ms:20}") long initialBackoffMillis,
            @Value("${bacon.inventory.retry.max-backoff-ms:200}") long maxBackoffMillis) {
        this.maxAttempts = Math.max(maxAttempts, 1);
        this.initialBackoffMillis = Math.max(initialBackoffMillis, 1L);
        this.maxBackoffMillis = Math.max(maxBackoffMillis, this.initialBackoffMillis);
    }

    public <T> T execute(String operation, String businessKey, Supplier<T> action) {
        long backoffMillis = initialBackoffMillis;
        int attempt = 0;
        while (attempt < maxAttempts) {
            attempt++;
            try {
                T result = action.get();
                if (attempt > 1) {
                    Metrics.counter("bacon.inventory.write.retry.recovered.total", "operation", operation).increment();
                }
                return result;
            } catch (InventoryDomainException ex) {
                if (!isConcurrentModified(ex)) {
                    throw ex;
                }
                Metrics.counter("bacon.inventory.write.retry.conflict.total", "operation", operation).increment();
                if (attempt >= maxAttempts) {
                    Metrics.counter("bacon.inventory.write.retry.exhausted.total", "operation", operation).increment();
                    log.error("ALERT inventory write retry exhausted, operation={}, businessKey={}, attempts={}",
                            operation, businessKey, attempt, ex);
                    throw ex;
                }
                log.warn("Inventory write conflict retry, operation={}, businessKey={}, attempt={}, backoffMs={}",
                        operation, businessKey, attempt, backoffMillis);
                sleepBackoff(backoffMillis, operation, businessKey);
                backoffMillis = Math.min(backoffMillis * 2, maxBackoffMillis);
            }
        }
        throw new InventoryDomainException(InventoryErrorCode.INVENTORY_CONCURRENT_MODIFIED, "retry-exhausted");
    }

    private boolean isConcurrentModified(InventoryDomainException exception) {
        return InventoryErrorCode.INVENTORY_CONCURRENT_MODIFIED.code().equals(exception.getCode());
    }

    private void sleepBackoff(long backoffMillis, String operation, String businessKey) {
        try {
            Thread.sleep(backoffMillis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            log.error("ALERT inventory write retry interrupted, operation={}, businessKey={}",
                    operation, businessKey, interruptedException);
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_CONCURRENT_MODIFIED, "retry-interrupted");
        }
    }
}
