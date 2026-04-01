package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditOutboxRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryInventoryAuditOutboxRepositoryImpl implements InventoryAuditOutboxRepository {

    private final InMemoryInventoryRepositorySupport support;

    public InMemoryInventoryAuditOutboxRepositoryImpl(InMemoryInventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public void saveAuditOutbox(InventoryAuditOutbox outbox) {
        support.saveAuditOutbox(outbox);
    }

    @Override
    public List<InventoryAuditOutbox> findRetryableAuditOutbox(Instant now, int limit) {
        return support.findRetryableAuditOutbox(now, limit);
    }

    @Override
    public List<InventoryAuditOutbox> claimRetryableAuditOutbox(Instant now, int limit,
                                                                 String processingOwner, Instant leaseUntil) {
        return support.claimRetryableAuditOutbox(now, limit, processingOwner, leaseUntil);
    }

    @Override
    public int releaseExpiredAuditOutboxLease(Instant now) {
        return support.releaseExpiredAuditOutboxLease(now);
    }

    @Override
    public void updateAuditOutboxForRetry(Long outboxId, int retryCount, Instant nextRetryAt, String errorMessage,
                                          Instant updatedAt) {
        support.updateAuditOutboxForRetry(outboxId, retryCount, nextRetryAt, errorMessage, updatedAt);
    }

    @Override
    public boolean updateAuditOutboxForRetryClaimed(Long outboxId, String processingOwner, int retryCount,
                                                    Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        return support.updateAuditOutboxForRetryClaimed(outboxId, processingOwner, retryCount, nextRetryAt,
                errorMessage, updatedAt);
    }

    @Override
    public void markAuditOutboxDead(Long outboxId, int retryCount, String deadReason, Instant updatedAt) {
        support.markAuditOutboxDead(outboxId, retryCount, deadReason, updatedAt);
    }

    @Override
    public boolean markAuditOutboxDeadClaimed(Long outboxId, String processingOwner, int retryCount,
                                              String deadReason, Instant updatedAt) {
        return support.markAuditOutboxDeadClaimed(outboxId, processingOwner, retryCount, deadReason, updatedAt);
    }

    @Override
    public void deleteAuditOutbox(Long outboxId) {
        support.deleteAuditOutbox(outboxId);
    }

    @Override
    public boolean deleteAuditOutboxClaimed(Long outboxId, String processingOwner) {
        return support.deleteAuditOutboxClaimed(outboxId, processingOwner);
    }
}
