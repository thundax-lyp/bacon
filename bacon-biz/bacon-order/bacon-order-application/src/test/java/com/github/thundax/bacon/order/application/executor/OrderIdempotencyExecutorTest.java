package com.github.thundax.bacon.order.application.executor;

import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

    @Test
    void expiredProcessingShouldBeReclaimed() {
        InMemoryOrderIdempotencyRepository repository = new InMemoryOrderIdempotencyRepository();
        OrderIdempotencyExecutor executor = new OrderIdempotencyExecutor(repository);
        AtomicInteger executedTimes = new AtomicInteger(0);

        OrderIdempotencyRecord stale = new OrderIdempotencyRecord(1L, 1001L, "ORD-3", "PAY-3",
                OrderIdempotencyExecutor.EVENT_MARK_PAID, OrderIdempotencyRecord.STATUS_PROCESSING, 1, null,
                "stale-owner", Instant.now().minusSeconds(30), Instant.now().minusSeconds(60),
                Instant.now().minusSeconds(120), Instant.now().minusSeconds(60));
        repository.forcePut(stale);
        executor.execute(OrderIdempotencyExecutor.EVENT_MARK_PAID, 1001L, "ORD-3", "PAY-3",
                executedTimes::incrementAndGet);

        assertEquals(1, executedTimes.get());
    }

    @Test
    void concurrentDuplicateRequestsShouldExecuteBusinessActionOnlyOnce() throws InterruptedException {
        OrderIdempotencyExecutor executor = new OrderIdempotencyExecutor(new InMemoryOrderIdempotencyRepository());
        AtomicInteger executedTimes = new AtomicInteger(0);
        int threadCount = 8;
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        try {
            for (int i = 0; i < threadCount; i++) {
                pool.execute(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        executor.execute(OrderIdempotencyExecutor.EVENT_MARK_PAID, 1001L,
                                "ORD-CONCURRENT-1", "PAY-CONCURRENT-1",
                                executedTimes::incrementAndGet);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }
            ready.await(2, TimeUnit.SECONDS);
            start.countDown();
            done.await(5, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
        assertEquals(1, executedTimes.get());
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
                    OrderIdempotencyRecord.STATUS_PROCESSING, 1, null, record.getProcessingOwner(),
                    record.getLeaseUntil(), record.getClaimedAt(), Instant.now(), Instant.now());
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
                existing.setProcessingOwner(null);
                existing.setLeaseUntil(null);
                existing.setClaimedAt(null);
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
                existing.setProcessingOwner(null);
                existing.setLeaseUntil(null);
                existing.setClaimedAt(null);
                existing.setUpdatedAt(updatedAt);
                updated.incrementAndGet();
                return existing;
            });
            return updated.get() > 0;
        }

        @Override
        public boolean retryFromFailed(Long tenantId, String orderNo, String paymentNo, String eventType,
                                       String processingOwner, Instant leaseUntil, Instant claimedAt, Instant updatedAt) {
            AtomicInteger updated = new AtomicInteger(0);
            storage.computeIfPresent(keyOf(tenantId, orderNo, paymentNo, eventType), (key, existing) -> {
                if (!OrderIdempotencyRecord.STATUS_FAILED.equals(existing.getStatus())) {
                    return existing;
                }
                existing.setStatus(OrderIdempotencyRecord.STATUS_PROCESSING);
                existing.setAttemptCount(existing.getAttemptCount() + 1);
                existing.setLastError(null);
                existing.setProcessingOwner(processingOwner);
                existing.setLeaseUntil(leaseUntil);
                existing.setClaimedAt(claimedAt);
                existing.setUpdatedAt(updatedAt);
                updated.incrementAndGet();
                return existing;
            });
            return updated.get() > 0;
        }

        @Override
        public boolean claimExpiredProcessing(Long tenantId, String orderNo, String paymentNo, String eventType,
                                              String processingOwner, Instant leaseUntil, Instant claimedAt,
                                              Instant updatedAt) {
            AtomicInteger updated = new AtomicInteger(0);
            storage.computeIfPresent(keyOf(tenantId, orderNo, paymentNo, eventType), (key, existing) -> {
                if (!OrderIdempotencyRecord.STATUS_PROCESSING.equals(existing.getStatus())) {
                    return existing;
                }
                Instant existingLease = existing.getLeaseUntil();
                if (existingLease != null && existingLease.isAfter(claimedAt)) {
                    return existing;
                }
                existing.setProcessingOwner(processingOwner);
                existing.setLeaseUntil(leaseUntil);
                existing.setClaimedAt(claimedAt);
                existing.setUpdatedAt(updatedAt);
                updated.incrementAndGet();
                return existing;
            });
            return updated.get() > 0;
        }

        @Override
        public int recoverExpiredProcessing(Instant now, String recoverMessage) {
            AtomicInteger recovered = new AtomicInteger(0);
            storage.forEach((key, existing) -> {
                if (!OrderIdempotencyRecord.STATUS_PROCESSING.equals(existing.getStatus())) {
                    return;
                }
                Instant leaseUntil = existing.getLeaseUntil();
                if (leaseUntil != null && leaseUntil.isAfter(now)) {
                    return;
                }
                existing.setStatus(OrderIdempotencyRecord.STATUS_FAILED);
                existing.setLastError(recoverMessage);
                existing.setProcessingOwner(null);
                existing.setLeaseUntil(null);
                existing.setClaimedAt(null);
                existing.setUpdatedAt(now);
                recovered.incrementAndGet();
            });
            return recovered.get();
        }

        void forcePut(OrderIdempotencyRecord record) {
            storage.put(keyOf(record.getTenantId(), record.getOrderNo(), record.getPaymentNo(), record.getEventType()),
                    record);
        }

        private String keyOf(Long tenantId, String orderNo, String paymentNo, String eventType) {
            return tenantId + ":" + orderNo + ":" + normalizePaymentNo(paymentNo) + ":" + eventType;
        }

        private String normalizePaymentNo(String paymentNo) {
            return paymentNo == null ? "" : paymentNo;
        }
    }
}
