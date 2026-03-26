package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import java.time.Instant;
import java.util.List;

public interface InventoryAuditOutboxRepository {

    default void saveAuditOutbox(InventoryAuditOutbox outbox) {
    }

    default List<InventoryAuditOutbox> findRetryableAuditOutbox(Instant now, int limit) {
        return List.of();
    }

    default List<InventoryAuditOutbox> claimRetryableAuditOutbox(Instant now, int limit,
                                                                  String processingOwner, Instant leaseUntil) {
        return List.of();
    }

    default int releaseExpiredAuditOutboxLease(Instant now) {
        return 0;
    }

    default void updateAuditOutboxForRetry(Long outboxId, int retryCount, Instant nextRetryAt, String errorMessage,
                                           Instant updatedAt) {
    }

    default boolean updateAuditOutboxForRetryClaimed(Long outboxId, String processingOwner, int retryCount,
                                                     Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        return false;
    }

    default void markAuditOutboxDead(Long outboxId, int retryCount, String deadReason, Instant updatedAt) {
    }

    default boolean markAuditOutboxDeadClaimed(Long outboxId, String processingOwner, int retryCount,
                                               String deadReason, Instant updatedAt) {
        return false;
    }

    default void deleteAuditOutbox(Long outboxId) {
    }

    default boolean deleteAuditOutboxClaimed(Long outboxId, String processingOwner) {
        return false;
    }
}
