package com.github.thundax.bacon.order.infra.persistence.repositoryimpl;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(InMemoryOrderOutboxSupport.class)
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
