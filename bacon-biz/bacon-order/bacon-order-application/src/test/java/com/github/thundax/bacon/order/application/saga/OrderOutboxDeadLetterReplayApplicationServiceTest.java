package com.github.thundax.bacon.order.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.order.application.result.OrderOutboxDeadLetterReplayResult;
import com.github.thundax.bacon.order.application.saga.OrderOutboxActionExecutor;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxReplayStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderOutboxDeadLetterId;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxDeadLetterRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class OrderOutboxDeadLetterReplayApplicationServiceTest {

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldMarkReplaySucceededWhenActionExecuted() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        InMemoryOrderOutboxDeadLetterRepository repository = new InMemoryOrderOutboxDeadLetterRepository();
        OrderOutboxDeadLetter deadLetter = OrderOutboxDeadLetter.create(
                OrderOutboxDeadLetterId.of(1001L),
                OrderOutboxEvent.reconstruct(
                        com.github.thundax.bacon.order.domain.model.valueobject.OutboxId.of(2001L),
                        null,
                        com.github.thundax.bacon.common.commerce.valueobject.OrderNo.of("ORD-1"),
                        OrderOutboxEventType.RESERVE_STOCK,
                        "ORD-1:RESERVE",
                        "{}",
                        OrderOutboxStatus.DEAD,
                        7,
                        null,
                        null,
                        null,
                        null,
                        "boom",
                        "MAX_RETRIES_EXCEEDED",
                        Instant.parse("2026-04-01T10:00:00Z"),
                        Instant.parse("2026-04-01T10:00:00Z")),
                7,
                "boom",
                "MAX_RETRIES_EXCEEDED",
                Instant.parse("2026-04-01T10:00:00Z"));
        repository.insert(deadLetter);
        OrderOutboxDeadLetterReplayApplicationService service =
                new OrderOutboxDeadLetterReplayApplicationService(repository, new NoopOrderOutboxActionExecutor());

        OrderOutboxDeadLetterReplayResult result = service.replay(OrderOutboxDeadLetterId.of(1001L));

        assertEquals("SUCCESS", result.replayStatus());
        assertEquals(OrderOutboxReplayStatus.SUCCESS, repository.findById(OrderOutboxDeadLetterId.of(1001L))
                .orElseThrow()
                .getReplayStatus());
    }

    @Test
    void shouldMarkReplayFailedWhenActionExecutionThrows() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        InMemoryOrderOutboxDeadLetterRepository repository = new InMemoryOrderOutboxDeadLetterRepository();
        OrderOutboxDeadLetter deadLetter = OrderOutboxDeadLetter.reconstruct(
                OrderOutboxDeadLetterId.of(1002L),
                com.github.thundax.bacon.order.domain.model.valueobject.OutboxId.of(2002L),
                null,
                com.github.thundax.bacon.common.commerce.valueobject.OrderNo.of("ORD-2"),
                OrderOutboxEventType.CREATE_PAYMENT,
                "ORD-2:CREATE_PAYMENT",
                "{}",
                8,
                "failed",
                "MAX_RETRIES_EXCEEDED",
                Instant.parse("2026-04-01T10:00:00Z"),
                OrderOutboxReplayStatus.FAILED,
                1,
                Instant.parse("2026-04-01T11:00:00Z"),
                "FAILED",
                Instant.parse("2026-04-01T10:00:00Z"),
                Instant.parse("2026-04-01T11:00:00Z"));
        repository.insert(deadLetter);
        OrderOutboxDeadLetterReplayApplicationService service =
                new OrderOutboxDeadLetterReplayApplicationService(repository, new FailingOrderOutboxActionExecutor());

        OrderOutboxDeadLetterReplayResult result = service.replay(OrderOutboxDeadLetterId.of(1002L));

        assertEquals("FAILED", result.replayStatus());
        assertEquals(OrderOutboxReplayStatus.FAILED, repository.findById(OrderOutboxDeadLetterId.of(1002L))
                .orElseThrow()
                .getReplayStatus());
    }

    private static final class InMemoryOrderOutboxDeadLetterRepository implements OrderOutboxDeadLetterRepository {

        private final Map<OrderOutboxDeadLetterId, OrderOutboxDeadLetter> storage = new HashMap<>();

        @Override
        public void insert(OrderOutboxDeadLetter deadLetter) {
            storage.put(deadLetter.getId(), deadLetter);
        }

        @Override
        public Optional<OrderOutboxDeadLetter> findById(OrderOutboxDeadLetterId id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public void markReplaySucceeded(OrderOutboxDeadLetterId id, Instant replayedAt, String message) {
            storage.get(id).markReplaySucceeded(replayedAt, message);
        }

        @Override
        public void markReplayFailed(OrderOutboxDeadLetterId id, Instant replayedAt, String message) {
            storage.get(id).markReplayFailed(replayedAt, message);
        }

        @Override
        public void markReplayPending(OrderOutboxDeadLetterId id, String message, Instant updatedAt) {
            storage.get(id).markReplayPending(message, updatedAt);
        }
    }

    private static class NoopOrderOutboxActionExecutor extends OrderOutboxActionExecutor {

        private NoopOrderOutboxActionExecutor() {
            super(null, null, null, null, null);
        }

        @Override
        public void executeClaimed(OrderOutboxEvent event) {}
    }

    private static final class FailingOrderOutboxActionExecutor extends NoopOrderOutboxActionExecutor {

        @Override
        public void executeClaimed(OrderOutboxEvent event) {
            throw new IllegalStateException("replay failed");
        }
    }
}
