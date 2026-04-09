package com.github.thundax.bacon.inventory.application.support;

import com.github.thundax.bacon.common.core.util.WriteConflictRetrier;
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

    private final WriteConflictRetrier writeConflictRetrier;

    public InventoryWriteRetrier() {
        this(6, 20L, 500L);
    }

    @Autowired
    public InventoryWriteRetrier(
            @Value("${bacon.inventory.retry.max-attempts:3}") int maxAttempts,
            @Value("${bacon.inventory.retry.initial-backoff-ms:20}") long initialBackoffMillis,
            @Value("${bacon.inventory.retry.max-backoff-ms:200}") long maxBackoffMillis) {
        this.writeConflictRetrier = new WriteConflictRetrier(maxAttempts, initialBackoffMillis, maxBackoffMillis);
    }

    public <T> T execute(String operation, String businessKey, Supplier<T> action) {
        try {
            // 这里只重试“明确识别为并发写冲突”的异常，其它业务异常直接原样抛出，避免把不可重试错误误当成临时冲突。
            return writeConflictRetrier.execute(
                    action, this::isConcurrentModified, new InventoryRetryMetricsListener(operation, businessKey));
        } catch (IllegalStateException exception) {
            if ("write-conflict-retry-interrupted".equals(exception.getMessage())) {
                throw new InventoryDomainException(
                        InventoryErrorCode.INVENTORY_CONCURRENT_MODIFIED, "retry-interrupted", exception);
            }
            throw exception;
        }
    }

    private boolean isConcurrentModified(RuntimeException exception) {
        if (!(exception instanceof InventoryDomainException inventoryDomainException)) {
            return false;
        }
        return InventoryErrorCode.INVENTORY_CONCURRENT_MODIFIED.code().equals(inventoryDomainException.getCode());
    }

    private static final class InventoryRetryMetricsListener implements WriteConflictRetrier.RetryListener {

        private final String operation;
        private final String businessKey;

        private InventoryRetryMetricsListener(String operation, String businessKey) {
            this.operation = operation;
            this.businessKey = businessKey;
        }

        @Override
        public void onConflict(int attempt, RuntimeException exception) {
            Metrics.counter("bacon.inventory.write.retry.conflict.total", "operation", operation)
                    .increment();
        }

        @Override
        public void onRetry(int attempt, long backoffMillis, RuntimeException exception) {
            log.warn(
                    "Inventory write conflict retry, operation={}, businessKey={}, attempt={}, backoffMs={}",
                    operation,
                    businessKey,
                    attempt,
                    backoffMillis);
        }

        @Override
        public void onRecovered(int attempt) {
            Metrics.counter("bacon.inventory.write.retry.recovered.total", "operation", operation)
                    .increment();
        }

        @Override
        public void onExhausted(int attempt, RuntimeException exception) {
            Metrics.counter("bacon.inventory.write.retry.exhausted.total", "operation", operation)
                    .increment();
            log.error(
                    "ALERT inventory write retry exhausted, operation={}, businessKey={}, attempts={}",
                    operation,
                    businessKey,
                    attempt,
                    exception);
        }

        @Override
        public void onInterrupted(
                int attempt, long backoffMillis, RuntimeException cause, InterruptedException interruptedException) {
            log.error(
                    "ALERT inventory write retry interrupted, operation={}, businessKey={}",
                    operation,
                    businessKey,
                    interruptedException);
        }
    }
}
