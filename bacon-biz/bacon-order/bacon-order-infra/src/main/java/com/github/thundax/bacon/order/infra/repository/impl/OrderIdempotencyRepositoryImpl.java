package com.github.thundax.bacon.order.infra.repository.impl;

import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class OrderIdempotencyRepositoryImpl implements OrderIdempotencyRepository {

    private final OrderIdempotencyRepositorySupport support;

    public OrderIdempotencyRepositoryImpl(OrderIdempotencyRepositorySupport support) {
        this.support = support;
    }

    @Override
    public boolean insertProcessing(OrderIdempotencyRecord record) {
        return support.insertProcessing(record);
    }

    @Override
    public boolean claimExpiredProcessing(
            OrderIdempotencyRecordKey key,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            Instant updatedAt) {
        return support.claimExpiredProcessing(key, processingOwner, leaseUntil, claimedAt, updatedAt);
    }

    @Override
    public Optional<OrderIdempotencyRecord> findByBusinessKey(OrderIdempotencyRecordKey key) {
        return support.findByBusinessKey(key);
    }

    @Override
    public boolean markSuccess(OrderIdempotencyRecordKey key, Instant updatedAt) {
        return support.markSuccess(key, updatedAt);
    }

    @Override
    public boolean markFailed(OrderIdempotencyRecordKey key, String lastError, Instant updatedAt) {
        return support.markFailed(key, lastError, updatedAt);
    }

    @Override
    public boolean recoverFromFailed(OrderIdempotencyRecordKey key, Instant updatedAt) {
        return support.recoverFromFailed(key, updatedAt);
    }

    @Override
    public boolean recoverFromFailed(
            OrderIdempotencyRecordKey key,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            Instant updatedAt) {
        return support.recoverFromFailed(key, processingOwner, leaseUntil, claimedAt, updatedAt);
    }

    @Override
    public int recoverExpiredProcessing(Instant now, String recoverMessage) {
        return support.recoverExpiredProcessing(now, recoverMessage);
    }
}
