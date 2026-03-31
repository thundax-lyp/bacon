package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class InMemoryOrderOutboxSupport {

    private final AtomicLong outboxIdGenerator = new AtomicLong(1000L);
    private final AtomicLong deadLetterIdGenerator = new AtomicLong(1000L);
    private final Map<Long, OrderOutboxEvent> outboxStorage = new ConcurrentHashMap<>();
    private final Map<Long, OrderOutboxDeadLetter> deadLetterStorage = new ConcurrentHashMap<>();

    public synchronized void saveOutboxEvent(OrderOutboxEvent event) {
        Instant now = Instant.now();
        if (event.getId() == null) {
            event.setId(outboxIdGenerator.getAndIncrement());
        }
        if (event.getStatus() == null) {
            event.setStatus(OrderOutboxEvent.STATUS_NEW);
        }
        if (event.getRetryCount() == null) {
            event.setRetryCount(0);
        }
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(now);
        }
        event.setUpdatedAt(now);
        outboxStorage.put(event.getId(), copy(event));
    }

    public synchronized List<OrderOutboxEvent> claimRetryableOutbox(Instant now, int limit, String processingOwner,
                                                                     Instant leaseUntil) {
        List<OrderOutboxEvent> candidates = outboxStorage.values().stream()
                .filter(event -> OrderOutboxEvent.STATUS_NEW.equals(event.getStatus())
                        || OrderOutboxEvent.STATUS_RETRYING.equals(event.getStatus()))
                .filter(event -> event.getNextRetryAt() == null || !event.getNextRetryAt().isAfter(now))
                .sorted(Comparator.comparing(OrderOutboxEvent::getCreatedAt)
                        .thenComparing(OrderOutboxEvent::getId))
                .limit(Math.max(limit, 1))
                .toList();
        List<OrderOutboxEvent> claimed = new ArrayList<>(candidates.size());
        for (OrderOutboxEvent candidate : candidates) {
            OrderOutboxEvent stored = outboxStorage.get(candidate.getId());
            if (stored == null) {
                continue;
            }
            if (!(OrderOutboxEvent.STATUS_NEW.equals(stored.getStatus())
                    || OrderOutboxEvent.STATUS_RETRYING.equals(stored.getStatus()))) {
                continue;
            }
            if (stored.getNextRetryAt() != null && stored.getNextRetryAt().isAfter(now)) {
                continue;
            }
            stored.setStatus(OrderOutboxEvent.STATUS_PROCESSING);
            stored.setProcessingOwner(processingOwner);
            stored.setLeaseUntil(leaseUntil);
            stored.setClaimedAt(now);
            stored.setUpdatedAt(now);
            claimed.add(copy(stored));
        }
        return List.copyOf(claimed);
    }

    public synchronized int releaseExpiredLease(Instant now) {
        int updated = 0;
        for (OrderOutboxEvent event : outboxStorage.values()) {
            if (OrderOutboxEvent.STATUS_PROCESSING.equals(event.getStatus())
                    && event.getLeaseUntil() != null
                    && !event.getLeaseUntil().isAfter(now)) {
                event.setStatus(OrderOutboxEvent.STATUS_RETRYING);
                event.setProcessingOwner(null);
                event.setLeaseUntil(null);
                event.setClaimedAt(null);
                event.setUpdatedAt(now);
                updated++;
            }
        }
        return updated;
    }

    public synchronized boolean markRetryingClaimed(Long outboxId, String processingOwner, int retryCount,
                                                    Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        OrderOutboxEvent event = outboxStorage.get(outboxId);
        if (event == null || !isClaimedBy(event, processingOwner)) {
            return false;
        }
        event.setStatus(OrderOutboxEvent.STATUS_RETRYING);
        event.setRetryCount(retryCount);
        event.setNextRetryAt(nextRetryAt);
        event.setErrorMessage(truncate(errorMessage));
        event.setProcessingOwner(null);
        event.setLeaseUntil(null);
        event.setClaimedAt(null);
        event.setUpdatedAt(updatedAt);
        return true;
    }

    public synchronized boolean markDeadClaimed(Long outboxId, String processingOwner, int retryCount,
                                                String deadReason, String errorMessage, Instant updatedAt) {
        OrderOutboxEvent event = outboxStorage.get(outboxId);
        if (event == null || !isClaimedBy(event, processingOwner)) {
            return false;
        }
        event.setStatus(OrderOutboxEvent.STATUS_DEAD);
        event.setRetryCount(retryCount);
        event.setDeadReason(deadReason);
        event.setErrorMessage(truncate(errorMessage));
        event.setProcessingOwner(null);
        event.setLeaseUntil(null);
        event.setClaimedAt(null);
        event.setUpdatedAt(updatedAt);
        return true;
    }

    public synchronized boolean deleteClaimed(Long outboxId, String processingOwner) {
        OrderOutboxEvent event = outboxStorage.get(outboxId);
        if (event == null || !isClaimedBy(event, processingOwner)) {
            return false;
        }
        outboxStorage.remove(outboxId);
        return true;
    }

    public synchronized void saveDeadLetter(OrderOutboxDeadLetter deadLetter) {
        Instant now = Instant.now();
        if (deadLetter.getId() == null) {
            deadLetter.setId(deadLetterIdGenerator.getAndIncrement());
        }
        if (deadLetter.getReplayStatus() == null) {
            deadLetter.setReplayStatus(OrderOutboxDeadLetter.REPLAY_STATUS_PENDING);
        }
        if (deadLetter.getReplayCount() == null) {
            deadLetter.setReplayCount(0);
        }
        if (deadLetter.getCreatedAt() == null) {
            deadLetter.setCreatedAt(now);
        }
        deadLetter.setUpdatedAt(now);
        deadLetterStorage.put(deadLetter.getId(), copy(deadLetter));
    }

    private boolean isClaimedBy(OrderOutboxEvent event, String processingOwner) {
        return OrderOutboxEvent.STATUS_PROCESSING.equals(event.getStatus())
                && processingOwner.equals(event.getProcessingOwner());
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }

    private OrderOutboxEvent copy(OrderOutboxEvent source) {
        return new OrderOutboxEvent(source.getId(), source.getTenantId(), source.getOrderNo(), source.getEventType(),
                source.getBusinessKey(), source.getPayload(), source.getStatus(), source.getRetryCount(),
                source.getNextRetryAt(), source.getProcessingOwner(), source.getLeaseUntil(), source.getClaimedAt(),
                source.getErrorMessage(), source.getDeadReason(), source.getCreatedAt(), source.getUpdatedAt());
    }

    private OrderOutboxDeadLetter copy(OrderOutboxDeadLetter source) {
        return new OrderOutboxDeadLetter(source.getId(), source.getOutboxId(), source.getTenantId(),
                source.getOrderNo(), source.getEventType(), source.getBusinessKey(), source.getPayload(),
                source.getRetryCount(), source.getErrorMessage(), source.getDeadReason(), source.getDeadAt(),
                source.getReplayStatus(), source.getReplayCount(), source.getLastReplayAt(),
                source.getLastReplayMessage(), source.getCreatedAt(), source.getUpdatedAt());
    }
}
