package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import java.time.Instant;
import java.util.List;

public interface OrderOutboxRepository {

    default void saveOutboxEvent(OrderOutboxEvent event) {
    }

    default List<OrderOutboxEvent> claimRetryableOutbox(Instant now, int limit,
                                                         String processingOwner, Instant leaseUntil) {
        return List.of();
    }

    default int releaseExpiredLease(Instant now) {
        return 0;
    }

    default boolean markRetryingClaimed(Long outboxId, String processingOwner, int retryCount,
                                        Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        return false;
    }

    default boolean markDeadClaimed(Long outboxId, String processingOwner, int retryCount,
                                    String deadReason, String errorMessage, Instant updatedAt) {
        return false;
    }

    default boolean deleteClaimed(Long outboxId, String processingOwner) {
        return false;
    }
}
