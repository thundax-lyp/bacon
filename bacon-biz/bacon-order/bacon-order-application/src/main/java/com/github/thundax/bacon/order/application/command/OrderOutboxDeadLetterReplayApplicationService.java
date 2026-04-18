package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.order.application.codec.OrderOutboxDeadLetterIdCodec;
import com.github.thundax.bacon.order.application.result.OrderOutboxDeadLetterReplayResult;
import com.github.thundax.bacon.order.application.saga.OrderOutboxActionExecutor;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderOutboxDeadLetterId;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxDeadLetterRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderOutboxDeadLetterReplayApplicationService {

    private final OrderOutboxDeadLetterRepository orderOutboxDeadLetterRepository;
    private final OrderOutboxActionExecutor orderOutboxActionExecutor;

    public OrderOutboxDeadLetterReplayApplicationService(
            OrderOutboxDeadLetterRepository orderOutboxDeadLetterRepository,
            OrderOutboxActionExecutor orderOutboxActionExecutor) {
        this.orderOutboxDeadLetterRepository = orderOutboxDeadLetterRepository;
        this.orderOutboxActionExecutor = orderOutboxActionExecutor;
    }

    @Transactional
    public OrderOutboxDeadLetterReplayResult replay(OrderOutboxDeadLetterId deadLetterId) {
        BaconContextHolder.requireTenantId();
        OrderOutboxDeadLetter deadLetter = orderOutboxDeadLetterRepository
                .findById(deadLetterId)
                .orElseThrow(() -> new NotFoundException("Order outbox dead letter not found: " + deadLetterId));
        if (deadLetter.isReplaySucceeded()) {
            return new OrderOutboxDeadLetterReplayResult(
                    OrderOutboxDeadLetterIdCodec.toValue(deadLetter.getId()),
                    deadLetter.getReplayStatus() == null ? null : deadLetter.getReplayStatus().value(),
                    "already-replayed");
        }
        Instant replayedAt = Instant.now();
        if (deadLetter.isReplayFailed()) {
            orderOutboxDeadLetterRepository.markReplayPending(deadLetterId, "RETRYING", replayedAt);
        }
        try {
            orderOutboxActionExecutor.executeClaimed(deadLetter.rebuildEvent());
            orderOutboxDeadLetterRepository.markReplaySucceeded(deadLetterId, replayedAt, "SUCCEEDED");
            return new OrderOutboxDeadLetterReplayResult(
                    OrderOutboxDeadLetterIdCodec.toValue(deadLetterId), "SUCCESS", "ok");
        } catch (RuntimeException ex) {
            orderOutboxDeadLetterRepository.markReplayFailed(deadLetterId, replayedAt, ex.getMessage());
            return new OrderOutboxDeadLetterReplayResult(
                    OrderOutboxDeadLetterIdCodec.toValue(deadLetterId), "FAILED", ex.getMessage());
        }
    }
}
