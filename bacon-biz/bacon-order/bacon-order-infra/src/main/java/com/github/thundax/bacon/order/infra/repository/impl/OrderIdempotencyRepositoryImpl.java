package com.github.thundax.bacon.order.infra.repository.impl;

import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import com.github.thundax.bacon.order.infra.persistence.repository.impl.OrderIdempotencyRepositorySupport;
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
    public boolean createProcessing(OrderIdempotencyRecord record) {
        return support.createProcessing(record);
    }

    @Override
    public boolean claimExpiredProcessing(OrderIdempotencyRecordKey key,
                                          String processingOwner, Instant leaseUntil, Instant claimedAt,
                                          Instant updatedAt) {
        return support.claimExpiredProcessing(key, processingOwner, leaseUntil,
                claimedAt, updatedAt);
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
    public boolean retryFromFailed(OrderIdempotencyRecordKey key, Instant updatedAt) {
        return support.retryFromFailed(key, updatedAt);
    }

    @Override
    public boolean retryFromFailed(OrderIdempotencyRecordKey key,
                                   String processingOwner, Instant leaseUntil, Instant claimedAt, Instant updatedAt) {
        return support.retryFromFailed(key, processingOwner, leaseUntil,
                claimedAt, updatedAt);
    }

    @Override
    public int recoverExpiredProcessing(Instant now, String recoverMessage) {
        return support.recoverExpiredProcessing(now, recoverMessage);
    }
}
