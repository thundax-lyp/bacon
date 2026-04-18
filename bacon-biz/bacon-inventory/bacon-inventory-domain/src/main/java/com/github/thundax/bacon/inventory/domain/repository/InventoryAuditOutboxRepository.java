package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import java.time.Instant;
import java.util.List;

public interface InventoryAuditOutboxRepository {

    record TenantScopedAuditOutbox(TenantId tenantId, InventoryAuditOutbox outbox) {}

    default void insert(InventoryAuditOutbox outbox) {}

    default List<InventoryAuditOutbox> findRetryable(Instant now, int limit) {
        return List.of();
    }

    default List<TenantScopedAuditOutbox> claimRetryable(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
        return List.of();
    }

    default int releaseExpiredLease(Instant now) {
        return 0;
    }

    default void updateForRetry(
            OutboxId outboxId, int retryCount, Instant nextRetryAt, String errorMessage, Instant updatedAt) {}

    default boolean updateForRetryClaimed(
            OutboxId outboxId,
            String processingOwner,
            int retryCount,
            Instant nextRetryAt,
            String errorMessage,
            Instant updatedAt) {
        return false;
    }

    default void markDead(OutboxId outboxId, int retryCount, String deadReason, Instant updatedAt) {}

    default boolean markDeadClaimed(
            OutboxId outboxId, String processingOwner, int retryCount, String deadReason, Instant updatedAt) {
        return false;
    }

    default void delete(OutboxId outboxId) {}

    default boolean deleteClaimed(OutboxId outboxId, String processingOwner) {
        return false;
    }
}
