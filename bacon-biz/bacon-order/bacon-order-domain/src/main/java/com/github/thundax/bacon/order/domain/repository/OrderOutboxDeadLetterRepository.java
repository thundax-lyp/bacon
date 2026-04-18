package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderOutboxDeadLetterId;
import java.time.Instant;
import java.util.Optional;

public interface OrderOutboxDeadLetterRepository {

    default void insert(OrderOutboxDeadLetter deadLetter) {}

    default Optional<OrderOutboxDeadLetter> findById(OrderOutboxDeadLetterId id) {
        return Optional.empty();
    }

    default void markReplaySucceeded(OrderOutboxDeadLetterId id, Instant replayedAt, String message) {}

    default void markReplayFailed(OrderOutboxDeadLetterId id, Instant replayedAt, String message) {}

    default void markReplayPending(OrderOutboxDeadLetterId id, String message, Instant updatedAt) {}
}
