package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryOrderIdempotencyRepositoryImpl implements OrderIdempotencyRepository {

    private final Map<String, OrderIdempotencyRecord> storage = new ConcurrentHashMap<>();

    @Override
    public boolean createProcessing(OrderIdempotencyRecord record) {
        String key = businessKey(valueOf(record.getOrderNo()), record.getEventType());
        OrderIdempotencyRecord created = copy(record);
        return storage.putIfAbsent(key, created) == null;
    }

    @Override
    public boolean claimExpiredProcessing(
            OrderIdempotencyRecordKey key,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            Instant updatedAt) {
        return storage.computeIfPresent(
                        businessKey(key.orderNo().value(), key.eventType()),
                        (mapKey, record) -> {
                            if (record.getStatus() != OrderIdempotencyStatus.PROCESSING) {
                                return record;
                            }
                            if (record.getLeaseUntil() != null
                                    && !record.getLeaseUntil().isBefore(claimedAt)) {
                                return record;
                            }
                            record.setProcessingOwner(processingOwner);
                            record.setLeaseUntil(leaseUntil);
                            record.setClaimedAt(claimedAt);
                            record.setUpdatedAt(updatedAt);
                            return record;
                        })
                != null;
    }

    @Override
    public Optional<OrderIdempotencyRecord> findByBusinessKey(OrderIdempotencyRecordKey key) {
        return Optional.ofNullable(storage.get(businessKey(key.orderNo().value(), key.eventType())))
                .map(this::copy);
    }

    @Override
    public boolean markSuccess(OrderIdempotencyRecordKey key, Instant updatedAt) {
        return updateStatus(key, OrderIdempotencyStatus.SUCCESS, null, updatedAt);
    }

    @Override
    public boolean markFailed(OrderIdempotencyRecordKey key, String lastError, Instant updatedAt) {
        return updateStatus(key, OrderIdempotencyStatus.FAILED, lastError, updatedAt);
    }

    @Override
    public boolean retryFromFailed(OrderIdempotencyRecordKey key, Instant updatedAt) {
        return retryFromFailed(key, null, null, null, updatedAt);
    }

    @Override
    public boolean retryFromFailed(
            OrderIdempotencyRecordKey key,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            Instant updatedAt) {
        return storage.computeIfPresent(
                        businessKey(key.orderNo().value(), key.eventType()),
                        (mapKey, record) -> {
                            if (record.getStatus() != OrderIdempotencyStatus.FAILED) {
                                return record;
                            }
                            record.setStatus(OrderIdempotencyStatus.PROCESSING);
                            record.setAttemptCount(
                                    (record.getAttemptCount() == null ? 0 : record.getAttemptCount()) + 1);
                            record.setProcessingOwner(processingOwner);
                            record.setLeaseUntil(leaseUntil);
                            record.setClaimedAt(claimedAt);
                            record.setLastError(null);
                            record.setUpdatedAt(updatedAt);
                            return record;
                        })
                != null;
    }

    @Override
    public int recoverExpiredProcessing(Instant now, String recoverMessage) {
        int recovered = 0;
        for (OrderIdempotencyRecord record : storage.values()) {
            if (record.getStatus() == OrderIdempotencyStatus.PROCESSING
                    && record.getLeaseUntil() != null
                    && record.getLeaseUntil().isBefore(now)) {
                record.setStatus(OrderIdempotencyStatus.FAILED);
                record.setLastError(recoverMessage);
                record.setUpdatedAt(now);
                recovered++;
            }
        }
        return recovered;
    }

    private boolean updateStatus(
            OrderIdempotencyRecordKey key, OrderIdempotencyStatus status, String lastError, Instant updatedAt) {
        return storage.computeIfPresent(
                        businessKey(key.orderNo().value(), key.eventType()),
                        (mapKey, record) -> {
                            record.setStatus(status);
                            record.setLastError(lastError);
                            record.setUpdatedAt(updatedAt);
                            return record;
                        })
                != null;
    }

    private String businessKey(String orderNo, String eventType) {
        return orderNo + ":" + eventType;
    }

    private OrderIdempotencyRecord copy(OrderIdempotencyRecord source) {
        return OrderIdempotencyRecord.reconstruct(
                OrderIdempotencyRecordKey.of(source.getOrderNo(), source.getEventType()),
                source.getStatus(),
                source.getAttemptCount(),
                source.getLastError(),
                source.getProcessingOwner(),
                source.getLeaseUntil(),
                source.getClaimedAt(),
                source.getCreatedAt(),
                source.getUpdatedAt());
    }

    private String valueOf(OrderNo orderNo) {
        return orderNo == null ? null : orderNo.value();
    }
}
