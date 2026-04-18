package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.order.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderOutboxDeadLetterId;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class InMemoryOrderOutboxSupport {

    private static final DateTimeFormatter EVENT_CODE_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AtomicLong outboxIdGenerator = new AtomicLong(1000L);
    private final AtomicLong outboxEventCodeGenerator = new AtomicLong(1000L);
    private final AtomicLong deadLetterIdGenerator = new AtomicLong(1000L);
    private final Map<OutboxId, OrderOutboxEvent> outboxStorage = new ConcurrentHashMap<>();
    private final Map<OrderOutboxDeadLetterId, OrderOutboxDeadLetter> deadLetterStorage = new ConcurrentHashMap<>();

    public synchronized void insert(OrderOutboxEvent event) {
        Instant now = Instant.now();
        OrderOutboxEvent stored = OrderOutboxEvent.reconstruct(
                event.getId() == null ? OutboxId.of(outboxIdGenerator.getAndIncrement()) : event.getId(),
                event.getEventCode() == null ? generateEventCode() : event.getEventCode(),
                event.getOrderNo(),
                event.getEventType(),
                event.getBusinessKey(),
                event.getPayload(),
                event.getStatus() == null ? OrderOutboxStatus.NEW : event.getStatus(),
                event.getRetryCount() == null ? 0 : event.getRetryCount(),
                event.getNextRetryAt(),
                event.getProcessingOwner(),
                event.getLeaseUntil(),
                event.getClaimedAt(),
                event.getErrorMessage(),
                event.getDeadReason(),
                event.getCreatedAt() == null ? now : event.getCreatedAt(),
                now);
        outboxStorage.put(stored.getId(), copy(stored));
    }

    public synchronized List<OrderOutboxEvent> claim(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
        List<OrderOutboxEvent> candidates = outboxStorage.values().stream()
                .filter(event -> event.isClaimable(now))
                .sorted(Comparator.comparing(OrderOutboxEvent::getCreatedAt)
                        .thenComparing(
                                event -> event.getId() == null
                                        ? null
                                        : event.getId().value(),
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(Math.max(limit, 1))
                .toList();
        List<OrderOutboxEvent> claimed = new ArrayList<>(candidates.size());
        for (OrderOutboxEvent candidate : candidates) {
            OrderOutboxEvent stored = outboxStorage.get(candidate.getId());
            if (stored == null) {
                continue;
            }
            if (!stored.isClaimable(now)) {
                continue;
            }
            OrderOutboxEvent claimedEvent = copy(stored);
            claimedEvent.claim(processingOwner, leaseUntil, now);
            outboxStorage.put(claimedEvent.getId(), claimedEvent);
            claimed.add(copy(claimedEvent));
        }
        return List.copyOf(claimed);
    }

    public synchronized int releaseExpired(Instant now) {
        int updated = 0;
        for (OrderOutboxEvent event : List.copyOf(outboxStorage.values())) {
            if (event.getStatus() == OrderOutboxStatus.PROCESSING && event.isLeaseExpired(now)) {
                OrderOutboxEvent released = copy(event);
                released.releaseExpiredLease(now);
                outboxStorage.put(event.getId(), released);
                updated += 1;
            }
        }
        return updated;
    }

    public synchronized boolean markRetryClaimed(
            OutboxId outboxId,
            String processingOwner,
            int retryCount,
            Instant nextRetryAt,
            String errorMessage,
            Instant updatedAt) {
        OrderOutboxEvent event = outboxStorage.get(outboxId);
        if (event == null || !event.isClaimedBy(processingOwner)) {
            return false;
        }
        OrderOutboxEvent retrying = copy(event);
        retrying.markRetrying(processingOwner, nextRetryAt, errorMessage, updatedAt);
        outboxStorage.put(outboxId, retrying);
        return true;
    }

    public synchronized boolean markDeadClaimed(
            OutboxId outboxId,
            String processingOwner,
            int retryCount,
            String deadReason,
            String errorMessage,
            Instant updatedAt) {
        OrderOutboxEvent event = outboxStorage.get(outboxId);
        if (event == null || !event.isClaimedBy(processingOwner)) {
            return false;
        }
        OrderOutboxEvent dead = copy(event);
        dead.markDead(processingOwner, deadReason, errorMessage, updatedAt);
        outboxStorage.put(outboxId, dead);
        return true;
    }

    public synchronized boolean deleteClaimed(OutboxId outboxId, String processingOwner) {
        OrderOutboxEvent event = outboxStorage.get(outboxId);
        if (event == null || !event.isClaimedBy(processingOwner)) {
            return false;
        }
        outboxStorage.remove(outboxId);
        return true;
    }

    public synchronized void insert(OrderOutboxDeadLetter deadLetter) {
        OrderOutboxDeadLetter stored = OrderOutboxDeadLetter.reconstruct(
                OrderOutboxDeadLetterId.of(deadLetterIdGenerator.getAndIncrement()),
                deadLetter.getOutboxId(),
                deadLetter.getEventCode(),
                deadLetter.getOrderNo(),
                deadLetter.getEventType(),
                deadLetter.getBusinessKey(),
                deadLetter.getPayload(),
                deadLetter.getRetryCount(),
                deadLetter.getErrorMessage(),
                deadLetter.getDeadReason(),
                deadLetter.getDeadAt(),
                deadLetter.getReplayStatus(),
                deadLetter.getReplayCount(),
                deadLetter.getLastReplayAt(),
                deadLetter.getLastReplayMessage(),
                deadLetter.getCreatedAt(),
                deadLetter.getUpdatedAt());
        deadLetterStorage.put(stored.getId(), copy(stored));
    }

    public synchronized Optional<OrderOutboxDeadLetter> findDeadLetterById(OrderOutboxDeadLetterId id) {
        return Optional.ofNullable(deadLetterStorage.get(id)).map(this::copy);
    }

    public synchronized void markDeadLetterReplaySucceeded(
            OrderOutboxDeadLetterId id, Instant replayedAt, String message) {
        OrderOutboxDeadLetter deadLetter = deadLetterStorage.get(id);
        if (deadLetter == null) {
            return;
        }
        deadLetter.markReplaySucceeded(replayedAt, message);
        deadLetterStorage.put(id, copy(deadLetter));
    }

    public synchronized void markDeadLetterReplayFailed(
            OrderOutboxDeadLetterId id, Instant replayedAt, String message) {
        OrderOutboxDeadLetter deadLetter = deadLetterStorage.get(id);
        if (deadLetter == null) {
            return;
        }
        deadLetter.markReplayFailed(replayedAt, message);
        deadLetterStorage.put(id, copy(deadLetter));
    }

    public synchronized void markDeadLetterReplayPending(
            OrderOutboxDeadLetterId id, String message, Instant updatedAt) {
        OrderOutboxDeadLetter deadLetter = deadLetterStorage.get(id);
        if (deadLetter == null) {
            return;
        }
        deadLetter.markReplayPending(message, updatedAt);
        deadLetterStorage.put(id, copy(deadLetter));
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }

    private EventCode generateEventCode() {
        long id = outboxEventCodeGenerator.getAndIncrement();
        String timestamp = LocalDateTime.now().format(EVENT_CODE_TIMESTAMP_FORMATTER);
        String suffix = String.format("%06d", Math.floorMod(id, 1_000_000L));
        return EventCode.of("EVT" + timestamp + "-" + suffix);
    }

    private OrderOutboxEvent copy(OrderOutboxEvent source) {
        return OrderOutboxEvent.reconstruct(
                source.getId(),
                source.getEventCode(),
                source.getOrderNo(),
                source.getEventType(),
                source.getBusinessKey(),
                source.getPayload(),
                source.getStatus(),
                source.getRetryCount(),
                source.getNextRetryAt(),
                source.getProcessingOwner(),
                source.getLeaseUntil(),
                source.getClaimedAt(),
                source.getErrorMessage(),
                source.getDeadReason(),
                source.getCreatedAt(),
                source.getUpdatedAt());
    }

    private OrderOutboxDeadLetter copy(OrderOutboxDeadLetter source) {
        return OrderOutboxDeadLetter.reconstruct(
                source.getId(),
                source.getOutboxId(),
                source.getEventCode(),
                source.getOrderNo(),
                source.getEventType(),
                source.getBusinessKey(),
                source.getPayload(),
                source.getRetryCount(),
                source.getErrorMessage(),
                source.getDeadReason(),
                source.getDeadAt(),
                source.getReplayStatus(),
                source.getReplayCount(),
                source.getLastReplayAt(),
                source.getLastReplayMessage(),
                source.getCreatedAt(),
                source.getUpdatedAt());
    }
}
