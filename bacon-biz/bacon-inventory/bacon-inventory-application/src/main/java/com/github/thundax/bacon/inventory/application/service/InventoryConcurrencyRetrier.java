package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InventoryConcurrencyRetrier {

    private final int maxAttempts;
    private final long initialBackoffMillis;
    private final long maxBackoffMillis;

    public InventoryConcurrencyRetrier() {
        this(6, 20L, 500L);
    }

    @Autowired
    public InventoryConcurrencyRetrier(
            @Value("${bacon.inventory.retry.max-attempts:3}") int maxAttempts,
            @Value("${bacon.inventory.retry.initial-backoff-ms:20}") long initialBackoffMillis,
            @Value("${bacon.inventory.retry.max-backoff-ms:200}") long maxBackoffMillis) {
        this.maxAttempts = Math.max(maxAttempts, 1);
        this.initialBackoffMillis = Math.max(initialBackoffMillis, 1L);
        this.maxBackoffMillis = Math.max(maxBackoffMillis, this.initialBackoffMillis);
    }

    public <T> T execute(Supplier<T> action) {
        long backoffMillis = initialBackoffMillis;
        int attempt = 0;
        while (attempt < maxAttempts) {
            attempt++;
            try {
                return action.get();
            } catch (InventoryDomainException ex) {
                if (!isConcurrentModified(ex) || attempt >= maxAttempts) {
                    throw ex;
                }
                sleepBackoff(backoffMillis);
                backoffMillis = Math.min(backoffMillis * 2, maxBackoffMillis);
            }
        }
        throw new InventoryDomainException(InventoryErrorCode.INVENTORY_CONCURRENT_MODIFIED, "retry-exhausted");
    }

    private boolean isConcurrentModified(InventoryDomainException exception) {
        return InventoryErrorCode.INVENTORY_CONCURRENT_MODIFIED.code().equals(exception.getCode());
    }

    private void sleepBackoff(long backoffMillis) {
        try {
            Thread.sleep(backoffMillis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new InventoryDomainException(InventoryErrorCode.INVENTORY_CONCURRENT_MODIFIED, "retry-interrupted");
        }
    }
}
