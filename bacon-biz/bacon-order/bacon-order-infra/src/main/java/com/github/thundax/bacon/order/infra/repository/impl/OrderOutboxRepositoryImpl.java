package com.github.thundax.bacon.order.infra.repository.impl;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxRepository;
import com.github.thundax.bacon.order.infra.persistence.repository.impl.OrderOutboxRepositorySupport;
import java.time.Instant;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class OrderOutboxRepositoryImpl implements OrderOutboxRepository {

    private final OrderOutboxRepositorySupport support;

    public OrderOutboxRepositoryImpl(OrderOutboxRepositorySupport support) {
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
    public boolean markRetryingClaimed(Long outboxId, String processingOwner, int retryCount, Instant nextRetryAt,
                                       String errorMessage, Instant updatedAt) {
        return support.markRetryingClaimed(outboxId, processingOwner, retryCount, nextRetryAt, errorMessage, updatedAt);
    }

    @Override
    public boolean markDeadClaimed(Long outboxId, String processingOwner, int retryCount, String deadReason,
                                   String errorMessage, Instant updatedAt) {
        return support.markDeadClaimed(outboxId, processingOwner, retryCount, deadReason, errorMessage, updatedAt);
    }

    @Override
    public boolean deleteClaimed(Long outboxId, String processingOwner) {
        return support.deleteClaimed(outboxId, processingOwner);
    }
}
