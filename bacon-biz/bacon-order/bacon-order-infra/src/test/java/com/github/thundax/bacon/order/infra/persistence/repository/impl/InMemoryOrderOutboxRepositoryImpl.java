package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryOrderOutboxRepositoryImpl implements OrderOutboxRepository {

    private final InMemoryOrderOutboxSupport support;

    public InMemoryOrderOutboxRepositoryImpl(InMemoryOrderOutboxSupport support) {
        this.support = support;
    }

    @Override
    public void saveOutboxEvent(OrderOutboxEvent event) {
        support.saveOutboxEvent(event);
    }

    @Override
    public List<OrderOutboxEvent> claimRetryableOutbox(Instant now, int limit, String processingOwner,
                                                       Instant leaseUntil) {
        return support.claimRetryableOutbox(now, limit, processingOwner, leaseUntil);
    }

    @Override
    public int releaseExpiredLease(Instant now) {
        return support.releaseExpiredLease(now);
    }

    @Override
    public boolean markRetryingClaimed(OutboxId outboxId, String processingOwner, int retryCount, Instant nextRetryAt,
                                       String errorMessage, Instant updatedAt) {
        return support.markRetryingClaimed(outboxId, processingOwner, retryCount, nextRetryAt, errorMessage, updatedAt);
    }

    @Override
    public boolean markDeadClaimed(OutboxId outboxId, String processingOwner, int retryCount, String deadReason,
                                   String errorMessage, Instant updatedAt) {
        return support.markDeadClaimed(outboxId, processingOwner, retryCount, deadReason, errorMessage, updatedAt);
    }

    @Override
    public boolean deleteClaimed(OutboxId outboxId, String processingOwner) {
        return support.deleteClaimed(outboxId, processingOwner);
    }
}
