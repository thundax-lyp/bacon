package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderIdempotencyRepository {

    default boolean insert(OrderIdempotencyRecord record) {
        return false;
    }

    default Optional<OrderIdempotencyRecord> findByKey(OrderIdempotencyRecordKey key) {
        return Optional.empty();
    }

    default boolean updateStatus(OrderIdempotencyRecord record, OrderIdempotencyStatus currentStatus) {
        return false;
    }

    default boolean updateStatus(
            OrderIdempotencyRecord record,
            OrderIdempotencyStatus currentStatus,
            Instant leaseExpiredBefore) {
        return false;
    }

    default List<OrderIdempotencyRecord> listExpiredProcessing(Instant now) {
        return List.of();
    }
}
