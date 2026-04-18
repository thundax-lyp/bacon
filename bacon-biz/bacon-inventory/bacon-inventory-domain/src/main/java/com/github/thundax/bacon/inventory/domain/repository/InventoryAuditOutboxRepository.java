package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import java.time.Instant;
import java.util.List;

public interface InventoryAuditOutboxRepository {

    record TenantScopedAuditOutbox(TenantId tenantId, InventoryAuditOutbox outbox) {}

    default void insertAuditOutbox(InventoryAuditOutbox outbox) {}

    default List<InventoryAuditOutbox> findRetryableAuditOutbox(Instant now, int limit) {
        return List.of();
    }

    default List<TenantScopedAuditOutbox> claimRetryableAuditOutbox(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
        return List.of();
    }

    default int releaseExpiredAuditOutboxLease(Instant now) {
        return 0;
    }

    default void updateAuditOutboxForRetry(
            OutboxId outboxId, int retryCount, Instant nextRetryAt, String errorMessage, Instant updatedAt) {}

    default boolean updateAuditOutboxForRetryClaimed(
            OutboxId outboxId,
            String processingOwner,
            int retryCount,
            Instant nextRetryAt,
            String errorMessage,
            Instant updatedAt) {
        return false;
    }

    default void markAuditOutboxDead(OutboxId outboxId, int retryCount, String deadReason, Instant updatedAt) {}

    default boolean markAuditOutboxDeadClaimed(
            OutboxId outboxId, String processingOwner, int retryCount, String deadReason, Instant updatedAt) {
        return false;
    }

    default void deleteAuditOutbox(OutboxId outboxId) {}

    default boolean deleteAuditOutboxClaimed(OutboxId outboxId, String processingOwner) {
        return false;
    }
}
