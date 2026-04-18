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
    public boolean insert(OrderIdempotencyRecord record) {
        return support.insert(record);
    }

    @Override
    public boolean claimExpired(
            OrderIdempotencyRecordKey key,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            Instant updatedAt) {
        return support.claimExpired(key, processingOwner, leaseUntil, claimedAt, updatedAt);
    }

    @Override
    public Optional<OrderIdempotencyRecord> findByKey(OrderIdempotencyRecordKey key) {
        return support.findByKey(key);
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
    public boolean recoverFailed(OrderIdempotencyRecordKey key, Instant updatedAt) {
        return support.recoverFailed(key, updatedAt);
    }

    @Override
    public boolean recoverFailed(
            OrderIdempotencyRecordKey key,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            Instant updatedAt) {
        return support.recoverFailed(key, processingOwner, leaseUntil, claimedAt, updatedAt);
    }

    @Override
    public int recoverExpired(Instant now, String recoverMessage) {
        return support.recoverExpired(now, recoverMessage);
    }
}
