package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryOrderIdempotencyRepositoryImpl implements OrderIdempotencyRepository {

    private final Map<String, OrderIdempotencyRecord> storage = new ConcurrentHashMap<>();

    @Override
    public boolean insert(OrderIdempotencyRecord record) {
        String key = businessKeyOf(record.getKey());
        OrderIdempotencyRecord created = copy(record);
        return storage.putIfAbsent(key, created) == null;
    }

    @Override
    public boolean updateStatus(OrderIdempotencyRecord record, OrderIdempotencyStatus currentStatus) {
        return updateStatus(record, currentStatus, null);
    }

    @Override
    public Optional<OrderIdempotencyRecord> findByKey(OrderIdempotencyRecordKey key) {
        return Optional.ofNullable(storage.get(businessKey(key.orderNo().value(), key.eventType())))
                .map(this::copy);
    }

    @Override
    public boolean updateStatus(
            OrderIdempotencyRecord record,
            OrderIdempotencyStatus currentStatus,
            Instant leaseExpiredBefore) {
        AtomicInteger updated = new AtomicInteger(0);
        storage.computeIfPresent(businessKeyOf(record.getKey()), (mapKey, existing) -> {
                    if (existing.getStatus() != currentStatus) {
                        return existing;
                    }
                    if (leaseExpiredBefore != null
                            && existing.getLeaseUntil() != null
                            && existing.getLeaseUntil().isAfter(leaseExpiredBefore)) {
                        return existing;
                    }
                    updated.incrementAndGet();
                    return copy(record);
                });
        return updated.get() > 0;
    }

    @Override
    public List<OrderIdempotencyRecord> listExpiredProcessing(Instant now) {
        return storage.values().stream()
                .filter(record -> record.getStatus() == OrderIdempotencyStatus.PROCESSING)
                .filter(record -> record.getLeaseUntil() != null && !record.getLeaseUntil().isAfter(now))
                .map(this::copy)
                .toList();
    }

    private String businessKey(String orderNo, String eventType) {
        return orderNo + ":" + eventType;
    }

    private OrderIdempotencyRecord copy(OrderIdempotencyRecord source) {
        return OrderIdempotencyRecord.reconstruct(
                source.getKey(),
                source.getStatus(),
                source.getAttemptCount(),
                source.getLastError(),
                source.getProcessingOwner(),
                source.getLeaseUntil(),
                source.getClaimedAt(),
                source.getCreatedAt(),
                source.getUpdatedAt());
    }

    private String businessKeyOf(OrderIdempotencyRecordKey key) {
        return businessKey(key == null || key.orderNo() == null ? null : key.orderNo().value(), key == null ? null : key.eventType());
    }
}
