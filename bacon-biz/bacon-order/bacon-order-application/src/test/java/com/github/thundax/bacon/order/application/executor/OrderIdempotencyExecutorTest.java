package com.github.thundax.bacon.order.application.executor;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderIdempotencyExecutorTest {

    @Test
    void duplicateSuccessShouldShortCircuit() {
        OrderIdempotencyExecutor executor = new OrderIdempotencyExecutor(new InMemoryOrderIdempotencyRepositoryImpl());
        AtomicInteger executedTimes = new AtomicInteger(0);

        executor.execute(OrderIdempotencyExecutor.EVENT_CANCEL, 1001L, "ORD-1", null, executedTimes::incrementAndGet);
        executor.execute(OrderIdempotencyExecutor.EVENT_CANCEL, 1001L, "ORD-1", null, executedTimes::incrementAndGet);

        assertEquals(1, executedTimes.get());
    }

    @Test
    void failedRecordShouldAllowRetry() {
        OrderIdempotencyExecutor executor = new OrderIdempotencyExecutor(new InMemoryOrderIdempotencyRepositoryImpl());
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
        InMemoryOrderIdempotencyRepositoryImpl repository = new InMemoryOrderIdempotencyRepositoryImpl();
        OrderIdempotencyExecutor executor = new OrderIdempotencyExecutor(repository);
        AtomicInteger executedTimes = new AtomicInteger(0);

        OrderIdempotencyRecord stale = new OrderIdempotencyRecord(
                1001L, "ORD-3", OrderIdempotencyExecutor.EVENT_MARK_PAID,
                OrderIdempotencyStatus.PROCESSING, 1, null,
                "stale-owner", Instant.now().minusSeconds(30), Instant.now().minusSeconds(60),
                Instant.now().minusSeconds(120), Instant.now().minusSeconds(60));
        repository.forcePut(stale);
        executor.execute(OrderIdempotencyExecutor.EVENT_MARK_PAID, 1001L, "ORD-3", "PAY-3",
                executedTimes::incrementAndGet);

        assertEquals(1, executedTimes.get());
    }

    @Test
    void concurrentDuplicateRequestsShouldExecuteBusinessActionOnlyOnce() throws InterruptedException {
        OrderIdempotencyExecutor executor = new OrderIdempotencyExecutor(new InMemoryOrderIdempotencyRepositoryImpl());
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

    private static final class InMemoryOrderIdempotencyRepositoryImpl implements OrderIdempotencyRepository {

        private final Map<String, OrderIdempotencyRecord> storage = new ConcurrentHashMap<>();

        @Override
        public boolean createProcessing(OrderIdempotencyRecord record) {
            String key = keyOf(record.getTenantIdValue(), record.getOrderNoValue(), record.getEventType());
            OrderIdempotencyRecord value = new OrderIdempotencyRecord(
                    record.getTenantIdValue(), record.getOrderNoValue(), record.getEventType(),
                    OrderIdempotencyStatus.PROCESSING, 1, null, record.getProcessingOwner(),
                    record.getLeaseUntil(), record.getClaimedAt(), Instant.now(), Instant.now());
            return storage.putIfAbsent(key, value) == null;
        }

        @Override
        public Optional<OrderIdempotencyRecord> findByBusinessKey(OrderIdempotencyRecordKey key) {
            return Optional.ofNullable(storage.get(keyOf(Long.valueOf(key.tenantId().value()), key.orderNo().value(),
                    key.eventType())));
        }

        @Override
        public boolean markSuccess(OrderIdempotencyRecordKey key, Instant updatedAt) {
            AtomicInteger updated = new AtomicInteger(0);
            storage.computeIfPresent(keyOf(Long.valueOf(key.tenantId().value()), key.orderNo().value(), key.eventType()),
                    (mapKey, existing) -> {
                if (existing.getStatus() != OrderIdempotencyStatus.PROCESSING) {
                    return existing;
                }
                existing.setStatus(OrderIdempotencyStatus.SUCCESS);
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
        public boolean markFailed(OrderIdempotencyRecordKey key, String lastError, Instant updatedAt) {
            AtomicInteger updated = new AtomicInteger(0);
            storage.computeIfPresent(keyOf(Long.valueOf(key.tenantId().value()), key.orderNo().value(), key.eventType()),
                    (mapKey, existing) -> {
                if (existing.getStatus() != OrderIdempotencyStatus.PROCESSING) {
                    return existing;
                }
                existing.setStatus(OrderIdempotencyStatus.FAILED);
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
        public boolean retryFromFailed(OrderIdempotencyRecordKey key,
                                       String processingOwner, Instant leaseUntil, Instant claimedAt, Instant updatedAt) {
            AtomicInteger updated = new AtomicInteger(0);
            storage.computeIfPresent(keyOf(Long.valueOf(key.tenantId().value()), key.orderNo().value(), key.eventType()),
                    (mapKey, existing) -> {
                if (existing.getStatus() != OrderIdempotencyStatus.FAILED) {
                    return existing;
                }
                existing.setStatus(OrderIdempotencyStatus.PROCESSING);
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
        public boolean claimExpiredProcessing(OrderIdempotencyRecordKey key,
                                              String processingOwner, Instant leaseUntil, Instant claimedAt,
                                              Instant updatedAt) {
            AtomicInteger updated = new AtomicInteger(0);
            storage.computeIfPresent(keyOf(Long.valueOf(key.tenantId().value()), key.orderNo().value(), key.eventType()),
                    (mapKey, existing) -> {
                if (existing.getStatus() != OrderIdempotencyStatus.PROCESSING) {
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
                if (existing.getStatus() != OrderIdempotencyStatus.PROCESSING) {
                    return;
                }
                Instant leaseUntil = existing.getLeaseUntil();
                if (leaseUntil != null && leaseUntil.isAfter(now)) {
                    return;
                }
                existing.setStatus(OrderIdempotencyStatus.FAILED);
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
            storage.put(keyOf(record.getTenantIdValue(), record.getOrderNoValue(), record.getEventType()),
                    record);
        }

        private String keyOf(Long tenantId, String orderNo, String eventType) {
            return tenantId + ":" + orderNo + ":" + eventType;
        }

        private TenantId toTenantId(Long tenantId) {
            return tenantId == null ? null : TenantId.of(tenantId);
        }

        private OrderNo toOrderNo(String orderNo) {
            return orderNo == null ? null : OrderNo.of(orderNo);
        }
    }
}
