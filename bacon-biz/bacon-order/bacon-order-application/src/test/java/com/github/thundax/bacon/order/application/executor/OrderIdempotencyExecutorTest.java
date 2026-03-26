package com.github.thundax.bacon.order.application.executor;

import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderIdempotencyExecutorTest {

    @Test
    void duplicateSuccessShouldShortCircuit() {
        OrderIdempotencyExecutor executor = new OrderIdempotencyExecutor(new InMemoryOrderIdempotencyRepository());
        AtomicInteger executedTimes = new AtomicInteger(0);

        executor.execute(OrderIdempotencyExecutor.EVENT_CANCEL, 1001L, "ORD-1", null, executedTimes::incrementAndGet);
        executor.execute(OrderIdempotencyExecutor.EVENT_CANCEL, 1001L, "ORD-1", null, executedTimes::incrementAndGet);

        assertEquals(1, executedTimes.get());
    }

    @Test
    void failedRecordShouldAllowRetry() {
        OrderIdempotencyExecutor executor = new OrderIdempotencyExecutor(new InMemoryOrderIdempotencyRepository());
        AtomicInteger executedTimes = new AtomicInteger(0);

        assertThrows(IllegalStateException.class, () -> executor.execute(OrderIdempotencyExecutor.EVENT_MARK_PAID,
                1001L, "ORD-2", "PAY-2", () -> {
                    if (executedTimes.incrementAndGet() == 1) {
                        throw new IllegalStateException("mock failure");
                    }
                }));
        executor.execute(OrderIdempotencyExecutor.EVENT_MARK_PAID, 1001L, "ORD-2", "PAY-2",
                executedTimes::incrementAndGet);

        assertEquals(2, executedTimes.get());
    }

    private static final class InMemoryOrderIdempotencyRepository implements OrderIdempotencyRepository {

        private final Map<String, OrderIdempotencyRecord> storage = new ConcurrentHashMap<>();
        private final AtomicLong idGenerator = new AtomicLong(1);

        @Override
        public boolean createProcessing(OrderIdempotencyRecord record) {
            String key = keyOf(record.getTenantId(), record.getOrderNo(), record.getPaymentNo(),
                    record.getEventType());
            OrderIdempotencyRecord value = new OrderIdempotencyRecord(idGenerator.getAndIncrement(), record.getTenantId(),
                    record.getOrderNo(), normalizePaymentNo(record.getPaymentNo()), record.getEventType(),
                    OrderIdempotencyRecord.STATUS_PROCESSING, 1, null, Instant.now(), Instant.now());
            return storage.putIfAbsent(key, value) == null;
        }

        @Override
        public Optional<OrderIdempotencyRecord> findByBusinessKey(Long tenantId, String orderNo, String paymentNo,
                                                                  String eventType) {
            return Optional.ofNullable(storage.get(keyOf(tenantId, orderNo, paymentNo, eventType)));
        }

        @Override
        public boolean markSuccess(Long tenantId, String orderNo, String paymentNo, String eventType,
                                   Instant updatedAt) {
            AtomicInteger updated = new AtomicInteger(0);
            storage.computeIfPresent(keyOf(tenantId, orderNo, paymentNo, eventType), (key, existing) -> {
                if (!OrderIdempotencyRecord.STATUS_PROCESSING.equals(existing.getStatus())) {
                    return existing;
                }
                existing.setStatus(OrderIdempotencyRecord.STATUS_SUCCESS);
                existing.setLastError(null);
                existing.setUpdatedAt(updatedAt);
                updated.incrementAndGet();
                return existing;
            });
            return updated.get() > 0;
        }

        @Override
        public boolean markFailed(Long tenantId, String orderNo, String paymentNo, String eventType, String lastError,
                                  Instant updatedAt) {
            AtomicInteger updated = new AtomicInteger(0);
            storage.computeIfPresent(keyOf(tenantId, orderNo, paymentNo, eventType), (key, existing) -> {
                if (!OrderIdempotencyRecord.STATUS_PROCESSING.equals(existing.getStatus())) {
                    return existing;
                }
                existing.setStatus(OrderIdempotencyRecord.STATUS_FAILED);
                existing.setLastError(lastError);
                existing.setUpdatedAt(updatedAt);
                updated.incrementAndGet();
                return existing;
            });
            return updated.get() > 0;
        }

        @Override
        public boolean retryFromFailed(Long tenantId, String orderNo, String paymentNo, String eventType,
                                       Instant updatedAt) {
            AtomicInteger updated = new AtomicInteger(0);
            storage.computeIfPresent(keyOf(tenantId, orderNo, paymentNo, eventType), (key, existing) -> {
                if (!OrderIdempotencyRecord.STATUS_FAILED.equals(existing.getStatus())) {
                    return existing;
                }
                existing.setStatus(OrderIdempotencyRecord.STATUS_PROCESSING);
                existing.setAttemptCount(existing.getAttemptCount() + 1);
                existing.setLastError(null);
                existing.setUpdatedAt(updatedAt);
                updated.incrementAndGet();
                return existing;
            });
            return updated.get() > 0;
        }

        private String keyOf(Long tenantId, String orderNo, String paymentNo, String eventType) {
            return tenantId + ":" + orderNo + ":" + normalizePaymentNo(paymentNo) + ":" + eventType;
        }

        private String normalizePaymentNo(String paymentNo) {
            return paymentNo == null ? "" : paymentNo;
        }
    }
}
