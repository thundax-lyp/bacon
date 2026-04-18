package com.github.thundax.bacon.order.application.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.AsyncTaskWrapper;
import com.github.thundax.bacon.order.application.codec.OrderIdempotencyRecordKeyCodec;
import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class OrderIdempotencyExecutorTest {

    @Test
    void duplicateSuccessShouldShortCircuit() {
        OrderIdempotencyExecutor executor = new OrderIdempotencyExecutor(new InMemoryOrderIdempotencyRepositoryImpl());
        AtomicInteger executedTimes = new AtomicInteger(0);

        executor.execute(OrderIdempotencyExecutor.EVENT_CANCEL, "ORD-1", executedTimes::incrementAndGet);
        executor.execute(OrderIdempotencyExecutor.EVENT_CANCEL, "ORD-1", executedTimes::incrementAndGet);

        assertEquals(1, executedTimes.get());
    }

    @Test
    void failedRecordShouldAllowRetry() {
        OrderIdempotencyExecutor executor = new OrderIdempotencyExecutor(new InMemoryOrderIdempotencyRepositoryImpl());
        AtomicInteger executedTimes = new AtomicInteger(0);

        assertThrows(
                IllegalStateException.class,
                () -> executor.execute(OrderIdempotencyExecutor.EVENT_MARK_PAID, "ORD-2", () -> {
                    if (executedTimes.incrementAndGet() == 1) {
                        throw new IllegalStateException("mock failure");
                    }
                }));
        executor.execute(OrderIdempotencyExecutor.EVENT_MARK_PAID, "ORD-2", executedTimes::incrementAndGet);

        assertEquals(2, executedTimes.get());
    }

    @Test
    void expiredProcessingShouldBeReclaimed() {
        InMemoryOrderIdempotencyRepositoryImpl repository = new InMemoryOrderIdempotencyRepositoryImpl();
        OrderIdempotencyExecutor executor = new OrderIdempotencyExecutor(repository);
        AtomicInteger executedTimes = new AtomicInteger(0);

        OrderIdempotencyRecord stale = OrderIdempotencyRecord.reconstruct(
                OrderIdempotencyRecordKey.of(OrderNo.of("ORD-3"), OrderIdempotencyExecutor.EVENT_MARK_PAID),
                OrderIdempotencyStatus.PROCESSING,
                1,
                null,
                "stale-owner",
                Instant.now().minusSeconds(30),
                Instant.now().minusSeconds(60),
                Instant.now().minusSeconds(120),
                Instant.now().minusSeconds(60));
        repository.forcePut(stale);
        executor.execute(OrderIdempotencyExecutor.EVENT_MARK_PAID, "ORD-3", executedTimes::incrementAndGet);

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
                pool.execute(AsyncTaskWrapper.wrap(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        executor.execute(
                                OrderIdempotencyExecutor.EVENT_MARK_PAID,
                                "ORD-CONCURRENT-1",
                                executedTimes::incrementAndGet);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                }));
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
        public boolean insert(OrderIdempotencyRecord record) {
            String key = keyOf(
                    OrderIdempotencyRecordKeyCodec.toOrderNoValue(record.getKey()),
                    OrderIdempotencyRecordKeyCodec.toEventTypeValue(record.getKey()));
            OrderIdempotencyRecord value = OrderIdempotencyRecord.reconstruct(
                    record.getKey(),
                    record.getStatus(),
                    record.getAttemptCount(),
                    record.getLastError(),
                    record.getProcessingOwner(),
                    record.getLeaseUntil(),
                    record.getClaimedAt(),
                    record.getCreatedAt(),
                    record.getUpdatedAt());
            return storage.putIfAbsent(key, value) == null;
        }

        @Override
        public Optional<OrderIdempotencyRecord> findByKey(OrderIdempotencyRecordKey key) {
            return Optional.ofNullable(storage.get(keyOf(key.orderNo().value(), key.eventType())))
                    .map(existing -> OrderIdempotencyRecord.reconstruct(
                            existing.getKey(),
                            existing.getStatus(),
                            existing.getAttemptCount(),
                            existing.getLastError(),
                            existing.getProcessingOwner(),
                            existing.getLeaseUntil(),
                            existing.getClaimedAt(),
                            existing.getCreatedAt(),
                            existing.getUpdatedAt()));
        }

        @Override
        public boolean updateStatus(OrderIdempotencyRecord record, OrderIdempotencyStatus currentStatus) {
            AtomicInteger updated = new AtomicInteger(0);
            storage.computeIfPresent(keyOf(record.getKey().orderNo().value(), record.getKey().eventType()), (mapKey, existing) -> {
                if (existing.getStatus() != currentStatus) {
                    return existing;
                }
                updated.incrementAndGet();
                return OrderIdempotencyRecord.reconstruct(
                        record.getKey(),
                        record.getStatus(),
                        record.getAttemptCount(),
                        record.getLastError(),
                        record.getProcessingOwner(),
                        record.getLeaseUntil(),
                        record.getClaimedAt(),
                        record.getCreatedAt(),
                        record.getUpdatedAt());
            });
            return updated.get() > 0;
        }

        @Override
        public boolean updateStatus(
                OrderIdempotencyRecord record,
                OrderIdempotencyStatus currentStatus,
                Instant leaseExpiredBefore) {
            AtomicInteger updated = new AtomicInteger(0);
            storage.computeIfPresent(keyOf(record.getKey().orderNo().value(), record.getKey().eventType()), (mapKey, existing) -> {
                if (existing.getStatus() != currentStatus) {
                    return existing;
                }
                Instant existingLease = existing.getLeaseUntil();
                if (leaseExpiredBefore != null && existingLease != null && existingLease.isAfter(leaseExpiredBefore)) {
                    return existing;
                }
                updated.incrementAndGet();
                return OrderIdempotencyRecord.reconstruct(
                        record.getKey(),
                        record.getStatus(),
                        record.getAttemptCount(),
                        record.getLastError(),
                        record.getProcessingOwner(),
                        record.getLeaseUntil(),
                        record.getClaimedAt(),
                        record.getCreatedAt(),
                        record.getUpdatedAt());
            });
            return updated.get() > 0;
        }

        @Override
        public List<OrderIdempotencyRecord> listExpiredProcessing(Instant now) {
            return storage.values().stream()
                    .filter(existing -> existing.getStatus() == OrderIdempotencyStatus.PROCESSING)
                    .filter(existing -> existing.getLeaseUntil() != null && !existing.getLeaseUntil().isAfter(now))
                    .map(existing -> OrderIdempotencyRecord.reconstruct(
                            existing.getKey(),
                            existing.getStatus(),
                            existing.getAttemptCount(),
                            existing.getLastError(),
                            existing.getProcessingOwner(),
                            existing.getLeaseUntil(),
                            existing.getClaimedAt(),
                            existing.getCreatedAt(),
                            existing.getUpdatedAt()))
                    .toList();
        }

        void forcePut(OrderIdempotencyRecord record) {
            storage.put(
                    keyOf(
                            OrderIdempotencyRecordKeyCodec.toOrderNoValue(record.getKey()),
                            OrderIdempotencyRecordKeyCodec.toEventTypeValue(record.getKey())),
                    record);
        }

        private String keyOf(String orderNo, String eventType) {
            return orderNo + ":" + eventType;
        }

        private OrderNo toOrderNo(String orderNo) {
            return orderNo == null ? null : OrderNo.of(orderNo);
        }

    }
}
