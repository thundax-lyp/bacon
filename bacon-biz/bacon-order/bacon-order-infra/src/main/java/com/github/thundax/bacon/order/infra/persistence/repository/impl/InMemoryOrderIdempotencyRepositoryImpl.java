package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "bacon.order.repository.mode", havingValue = "memory")
public class InMemoryOrderIdempotencyRepositoryImpl implements OrderIdempotencyRepository {

    private final Map<String, OrderIdempotencyRecord> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1000L);

    @Override
    public boolean createProcessing(OrderIdempotencyRecord record) {
        String key = businessKey(record.getTenantId(), record.getOrderNo(), record.getPaymentNo(), record.getEventType());
        OrderIdempotencyRecord created = copy(record);
        created.setId(idGenerator.getAndIncrement());
        return storage.putIfAbsent(key, created) == null;
    }

    @Override
    public boolean claimExpiredProcessing(Long tenantId, String orderNo, String paymentNo, String eventType,
                                          String processingOwner, Instant leaseUntil, Instant claimedAt,
                                          Instant updatedAt) {
        return storage.computeIfPresent(businessKey(tenantId, orderNo, paymentNo, eventType), (key, record) -> {
            if (!OrderIdempotencyRecord.STATUS_PROCESSING.equals(record.getStatus())) {
                return record;
            }
            if (record.getLeaseUntil() != null && !record.getLeaseUntil().isBefore(claimedAt)) {
                return record;
            }
            record.setProcessingOwner(processingOwner);
            record.setLeaseUntil(leaseUntil);
            record.setClaimedAt(claimedAt);
            record.setUpdatedAt(updatedAt);
            return record;
        }) != null;
    }

    @Override
    public Optional<OrderIdempotencyRecord> findByBusinessKey(Long tenantId, String orderNo,
                                                              String paymentNo, String eventType) {
        return Optional.ofNullable(storage.get(businessKey(tenantId, orderNo, paymentNo, eventType)))
                .map(this::copy);
    }

    @Override
    public boolean markSuccess(Long tenantId, String orderNo, String paymentNo, String eventType,
                               Instant updatedAt) {
        return updateStatus(tenantId, orderNo, paymentNo, eventType, OrderIdempotencyRecord.STATUS_SUCCESS,
                null, updatedAt);
    }

    @Override
    public boolean markFailed(Long tenantId, String orderNo, String paymentNo, String eventType,
                              String lastError, Instant updatedAt) {
        return updateStatus(tenantId, orderNo, paymentNo, eventType, OrderIdempotencyRecord.STATUS_FAILED,
                lastError, updatedAt);
    }

    @Override
    public boolean retryFromFailed(Long tenantId, String orderNo, String paymentNo, String eventType,
                                   Instant updatedAt) {
        return retryFromFailed(tenantId, orderNo, paymentNo, eventType, null, null, null, updatedAt);
    }

    @Override
    public boolean retryFromFailed(Long tenantId, String orderNo, String paymentNo, String eventType,
                                   String processingOwner, Instant leaseUntil, Instant claimedAt, Instant updatedAt) {
        return storage.computeIfPresent(businessKey(tenantId, orderNo, paymentNo, eventType), (key, record) -> {
            if (!OrderIdempotencyRecord.STATUS_FAILED.equals(record.getStatus())) {
                return record;
            }
            record.setStatus(OrderIdempotencyRecord.STATUS_PROCESSING);
            record.setAttemptCount((record.getAttemptCount() == null ? 0 : record.getAttemptCount()) + 1);
            record.setProcessingOwner(processingOwner);
            record.setLeaseUntil(leaseUntil);
            record.setClaimedAt(claimedAt);
            record.setLastError(null);
            record.setUpdatedAt(updatedAt);
            return record;
        }) != null;
    }

    @Override
    public int recoverExpiredProcessing(Instant now, String recoverMessage) {
        int recovered = 0;
        for (OrderIdempotencyRecord record : storage.values()) {
            if (OrderIdempotencyRecord.STATUS_PROCESSING.equals(record.getStatus())
                    && record.getLeaseUntil() != null
                    && record.getLeaseUntil().isBefore(now)) {
                record.setStatus(OrderIdempotencyRecord.STATUS_FAILED);
                record.setLastError(recoverMessage);
                record.setUpdatedAt(now);
                recovered++;
            }
        }
        return recovered;
    }

    private boolean updateStatus(Long tenantId, String orderNo, String paymentNo, String eventType,
                                 String status, String lastError, Instant updatedAt) {
        return storage.computeIfPresent(businessKey(tenantId, orderNo, paymentNo, eventType), (key, record) -> {
            record.setStatus(status);
            record.setLastError(lastError);
            record.setUpdatedAt(updatedAt);
            return record;
        }) != null;
    }

    private String businessKey(Long tenantId, String orderNo, String paymentNo, String eventType) {
        return tenantId + ":" + orderNo + ":" + paymentNo + ":" + eventType;
    }

    private OrderIdempotencyRecord copy(OrderIdempotencyRecord source) {
        return new OrderIdempotencyRecord(source.getId(), source.getTenantId(), source.getOrderNo(),
                source.getPaymentNo(), source.getEventType(), source.getStatus(), source.getAttemptCount(),
                source.getLastError(), source.getProcessingOwner(), source.getLeaseUntil(), source.getClaimedAt(),
                source.getCreatedAt(), source.getUpdatedAt());
    }
}
