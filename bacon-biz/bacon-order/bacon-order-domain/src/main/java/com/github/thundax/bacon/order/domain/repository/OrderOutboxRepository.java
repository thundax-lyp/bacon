package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.valueobject.OutboxId;
import java.time.Instant;
import java.util.List;

public interface OrderOutboxRepository {

    default void insert(OrderOutboxEvent event) {}

    default List<OrderOutboxEvent> claimRetryable(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
        return List.of();
    }

    default int releaseExpiredLease(Instant now) {
        return 0;
    }

    default boolean markRetryingClaimed(
            OutboxId outboxId,
            String processingOwner,
            int retryCount,
            Instant nextRetryAt,
            String errorMessage,
            Instant updatedAt) {
        return false;
    }

    default boolean markDeadClaimed(
            OutboxId outboxId,
            String processingOwner,
            int retryCount,
            String deadReason,
            String errorMessage,
            Instant updatedAt) {
        return false;
    }

    default boolean deleteClaimed(OutboxId outboxId, String processingOwner) {
        return false;
    }
}
