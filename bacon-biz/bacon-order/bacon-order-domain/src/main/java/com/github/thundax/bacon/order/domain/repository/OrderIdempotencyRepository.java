package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import java.time.Instant;
import java.util.Optional;

public interface OrderIdempotencyRepository {

    default boolean insert(OrderIdempotencyRecord record) {
        return false;
    }

    default boolean claimExpired(
            OrderIdempotencyRecordKey key,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            Instant updatedAt) {
        return false;
    }

    default Optional<OrderIdempotencyRecord> findByKey(OrderIdempotencyRecordKey key) {
        return Optional.empty();
    }

    default boolean markSuccess(OrderIdempotencyRecordKey key, Instant updatedAt) {
        return false;
    }

    default boolean markFailed(OrderIdempotencyRecordKey key, String lastError, Instant updatedAt) {
        return false;
    }

    default boolean recoverFailed(OrderIdempotencyRecordKey key, Instant updatedAt) {
        return false;
    }

    default boolean recoverFailed(
            OrderIdempotencyRecordKey key,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            Instant updatedAt) {
        return false;
    }

    default int recoverExpired(Instant now, String recoverMessage) {
        return 0;
    }
}
