package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditOutboxRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class InventoryAuditOutboxRepositoryImpl implements InventoryAuditOutboxRepository {

    private final InventoryRepositorySupport support;

    public InventoryAuditOutboxRepositoryImpl(InventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public void insert(InventoryAuditOutbox outbox) {
        support.insert(outbox);
    }

    @Override
    public List<InventoryAuditOutbox> findRetryable(Instant now, int limit) {
        return support.findRetryable(now, limit);
    }

    @Override
    public List<TenantScopedAuditOutbox> claimRetryable(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
        return support.claimRetryable(now, limit, processingOwner, leaseUntil);
    }

    @Override
    public int releaseExpiredLease(Instant now) {
        return support.releaseExpiredLease(now);
    }

    @Override
    public void updateForRetry(
            OutboxId outboxId, int retryCount, Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        support.updateForRetry(outboxId, retryCount, nextRetryAt, errorMessage, updatedAt);
    }

    @Override
    public boolean updateForRetryClaimed(
            OutboxId outboxId,
            String processingOwner,
            int retryCount,
            Instant nextRetryAt,
            String errorMessage,
            Instant updatedAt) {
        return support.updateForRetryClaimed(
                outboxId, processingOwner, retryCount, nextRetryAt, errorMessage, updatedAt);
    }

    @Override
    public void markDead(OutboxId outboxId, int retryCount, String deadReason, Instant updatedAt) {
        support.markDead(outboxId, retryCount, deadReason, updatedAt);
    }

    @Override
    public boolean markDeadClaimed(
            OutboxId outboxId, String processingOwner, int retryCount, String deadReason, Instant updatedAt) {
        return support.markDeadClaimed(outboxId, processingOwner, retryCount, deadReason, updatedAt);
    }

    @Override
    public void delete(OutboxId outboxId) {
        support.delete(outboxId);
    }

    @Override
    public boolean deleteClaimed(OutboxId outboxId, String processingOwner) {
        return support.deleteClaimed(outboxId, processingOwner);
    }
}
