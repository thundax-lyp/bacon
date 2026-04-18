package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderOutboxDeadLetterId;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxDeadLetterRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryOrderOutboxDeadLetterRepositoryImpl implements OrderOutboxDeadLetterRepository {

    private final InMemoryOrderOutboxSupport support;

    public InMemoryOrderOutboxDeadLetterRepositoryImpl(InMemoryOrderOutboxSupport support) {
        this.support = support;
    }

    @Override
    public void insert(OrderOutboxDeadLetter deadLetter) {
        support.insert(deadLetter);
    }

    @Override
    public Optional<OrderOutboxDeadLetter> findById(OrderOutboxDeadLetterId id) {
        return support.findDeadLetterById(id);
    }

    @Override
    public void markReplaySucceeded(OrderOutboxDeadLetterId id, Instant replayedAt, String message) {
        support.markDeadLetterReplaySucceeded(id, replayedAt, message);
    }

    @Override
    public void markReplayFailed(OrderOutboxDeadLetterId id, Instant replayedAt, String message) {
        support.markDeadLetterReplayFailed(id, replayedAt, message);
    }

    @Override
    public void markReplayPending(OrderOutboxDeadLetterId id, String message, Instant updatedAt) {
        support.markDeadLetterReplayPending(id, message, updatedAt);
    }
}
