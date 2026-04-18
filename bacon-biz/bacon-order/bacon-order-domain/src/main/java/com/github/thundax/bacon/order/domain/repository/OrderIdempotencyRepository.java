package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import java.time.Instant;
import java.util.Optional;

public interface OrderIdempotencyRepository {

    default boolean insertProcessing(OrderIdempotencyRecord record) {
        return false;
    }

    default boolean claimExpiredProcessing(
            OrderIdempotencyRecordKey key,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            Instant updatedAt) {
        return false;
    }

    default Optional<OrderIdempotencyRecord> findByBusinessKey(OrderIdempotencyRecordKey key) {
        return Optional.empty();
    }

    default boolean markSuccess(OrderIdempotencyRecordKey key, Instant updatedAt) {
        return false;
    }

    default boolean markFailed(OrderIdempotencyRecordKey key, String lastError, Instant updatedAt) {
        return false;
    }

    default boolean recoverFromFailed(OrderIdempotencyRecordKey key, Instant updatedAt) {
        return false;
    }

    default boolean recoverFromFailed(
            OrderIdempotencyRecordKey key,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            Instant updatedAt) {
        return false;
    }

    default int recoverExpiredProcessing(Instant now, String recoverMessage) {
        return 0;
    }
}
