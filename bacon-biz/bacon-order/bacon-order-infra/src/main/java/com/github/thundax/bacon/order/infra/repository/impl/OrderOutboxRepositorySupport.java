package com.github.thundax.bacon.order.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxReplayStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.order.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderOutboxDeadLetterId;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderOutboxDeadLetterPersistenceAssembler;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderOutboxEventPersistenceAssembler;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderOutboxDeadLetterDO;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderOutboxEventDO;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderOutboxDeadLetterMapper;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderOutboxEventMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
public class OrderOutboxRepositorySupport {

    private static final String OUTBOX_ID_BIZ_TAG = "order_outbox_id";
    private static final String OUTBOX_EVENT_CODE_BIZ_TAG = "order_outbox_event_code";
    private static final DateTimeFormatter EVENT_CODE_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderOutboxEventMapper outboxEventMapper;
    private final OrderOutboxDeadLetterMapper deadLetterMapper;
    private final IdGenerator idGenerator;
    private final OrderOutboxEventPersistenceAssembler orderOutboxEventPersistenceAssembler;
    private final OrderOutboxDeadLetterPersistenceAssembler orderOutboxDeadLetterPersistenceAssembler;

    public OrderOutboxRepositorySupport(
            OrderOutboxEventMapper outboxEventMapper,
            OrderOutboxDeadLetterMapper deadLetterMapper,
            IdGenerator idGenerator,
            OrderOutboxEventPersistenceAssembler orderOutboxEventPersistenceAssembler,
            OrderOutboxDeadLetterPersistenceAssembler orderOutboxDeadLetterPersistenceAssembler) {
        this.outboxEventMapper = outboxEventMapper;
        this.deadLetterMapper = deadLetterMapper;
        this.idGenerator = idGenerator;
        this.orderOutboxEventPersistenceAssembler = orderOutboxEventPersistenceAssembler;
        this.orderOutboxDeadLetterPersistenceAssembler = orderOutboxDeadLetterPersistenceAssembler;
    }

    public void insert(OrderOutboxEvent event) {
        OrderOutboxEventDO dataObject = orderOutboxEventPersistenceAssembler.toDataObject(event);
        Instant now = Instant.now();
        dataObject.setCreatedAt(dataObject.getCreatedAt() == null ? now : dataObject.getCreatedAt());
        dataObject.setUpdatedAt(now);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId(OUTBOX_ID_BIZ_TAG));
        }
        if (dataObject.getEventCode() == null || dataObject.getEventCode().isBlank()) {
            dataObject.setEventCode(generateEventCode().value());
        }
        // outbox 插入即进入可调度状态，后续所有处理权转移都通过 status + processingOwner + leaseUntil 控制。
        outboxEventMapper.insert(dataObject);
    }

    public List<OrderOutboxEvent> claim(Instant now, int limit, String processingOwner, Instant leaseUntil) {
        List<OrderOutboxEventDO> candidates = outboxEventMapper
                .selectList(Wrappers.<OrderOutboxEventDO>lambdaQuery()
                        .in(
                                OrderOutboxEventDO::getStatus,
                                OrderOutboxStatus.NEW.value(),
                                OrderOutboxStatus.RETRYING.value())
                        .and(wrapper -> wrapper.isNull(OrderOutboxEventDO::getNextRetryAt)
                                .or()
                                .le(OrderOutboxEventDO::getNextRetryAt, now))
                        .orderByAsc(OrderOutboxEventDO::getCreatedAt, OrderOutboxEventDO::getId)
                        .last("limit " + Math.max(limit * 3, limit)))
                .stream()
                .toList();
        if (candidates.isEmpty()) {
            return List.of();
        }
        List<OrderOutboxEvent> claimed = new ArrayList<>(limit);
        // 先粗查候选，再逐条 CAS 抢占，避免多节点并发扫描时把同一批事件都误认为可处理。
        for (OrderOutboxEventDO candidate : candidates) {
            if (claimed.size() >= limit) {
                break;
            }
            OrderOutboxEvent candidateEvent = orderOutboxEventPersistenceAssembler.toDomain(candidate);
            if (!candidateEvent.isClaimable(now)) {
                continue;
            }
            candidateEvent.claim(processingOwner, leaseUntil, now);
            int updated = outboxEventMapper.update(
                    null,
                    Wrappers.<OrderOutboxEventDO>lambdaUpdate()
                            .eq(OrderOutboxEventDO::getId, candidate.getId())
                            .in(
                                    OrderOutboxEventDO::getStatus,
                                    OrderOutboxStatus.NEW.value(),
                                    OrderOutboxStatus.RETRYING.value())
                            .and(wrapper -> wrapper.isNull(OrderOutboxEventDO::getNextRetryAt)
                                    .or()
                                    .le(OrderOutboxEventDO::getNextRetryAt, now))
                            .set(
                                    OrderOutboxEventDO::getStatus,
                                    candidateEvent.getStatus() == null
                                            ? null
                                            : candidateEvent.getStatus().value())
                            .set(OrderOutboxEventDO::getProcessingOwner, candidateEvent.getProcessingOwner())
                            .set(OrderOutboxEventDO::getLeaseUntil, candidateEvent.getLeaseUntil())
                            .set(OrderOutboxEventDO::getClaimedAt, candidateEvent.getClaimedAt())
                            .set(OrderOutboxEventDO::getUpdatedAt, candidateEvent.getUpdatedAt()));
            if (updated == 0) {
                continue;
            }
            OrderOutboxEventDO claimedDo = outboxEventMapper.selectById(candidate.getId());
            if (claimedDo != null) {
                claimed.add(orderOutboxEventPersistenceAssembler.toDomain(claimedDo));
            }
        }
        return List.copyOf(claimed);
    }

    public int releaseExpired(Instant now) {
        List<OrderOutboxEventDO> expired = outboxEventMapper.selectList(Wrappers.<OrderOutboxEventDO>lambdaQuery()
                .eq(OrderOutboxEventDO::getStatus, OrderOutboxStatus.PROCESSING.value())
                .le(OrderOutboxEventDO::getLeaseUntil, now));
        if (expired.isEmpty()) {
            return 0;
        }
        int updated = 0;
        for (OrderOutboxEventDO dataObject : expired) {
            OrderOutboxEvent event = orderOutboxEventPersistenceAssembler.toDomain(dataObject);
            if (!event.isLeaseExpired(now)) {
                continue;
            }
            event.releaseExpiredLease(now);
            updated += outboxEventMapper.update(
                            null,
                            Wrappers.<OrderOutboxEventDO>lambdaUpdate()
                                    .eq(OrderOutboxEventDO::getId, dataObject.getId())
                                    .eq(OrderOutboxEventDO::getStatus, OrderOutboxStatus.PROCESSING.value())
                                    .le(OrderOutboxEventDO::getLeaseUntil, now)
                                    .set(
                                            OrderOutboxEventDO::getStatus,
                                            event.getStatus() == null ? null : event.getStatus().value())
                                    .set(OrderOutboxEventDO::getProcessingOwner, event.getProcessingOwner())
                                    .set(OrderOutboxEventDO::getLeaseUntil, event.getLeaseUntil())
                                    .set(OrderOutboxEventDO::getClaimedAt, event.getClaimedAt())
                                    .set(OrderOutboxEventDO::getUpdatedAt, event.getUpdatedAt()))
                    > 0 ? 1 : 0;
        }
        return updated;
    }

    public boolean markRetryClaimed(
            OutboxId outboxId,
            String processingOwner,
            int retryCount,
            Instant nextRetryAt,
            String errorMessage,
            Instant updatedAt) {
        OrderOutboxEventDO dataObject = outboxId == null ? null : outboxEventMapper.selectById(outboxId.value());
        if (dataObject == null) {
            return false;
        }
        OrderOutboxEvent event = orderOutboxEventPersistenceAssembler.toDomain(dataObject);
        if (!event.isClaimedBy(processingOwner)) {
            return false;
        }
        event.markRetrying(processingOwner, nextRetryAt, errorMessage, updatedAt);
        return outboxEventMapper.update(
                        null,
                        Wrappers.<OrderOutboxEventDO>lambdaUpdate()
                                .eq(OrderOutboxEventDO::getId, outboxId == null ? null : outboxId.value())
                                .eq(OrderOutboxEventDO::getStatus, OrderOutboxStatus.PROCESSING.value())
                                .eq(OrderOutboxEventDO::getProcessingOwner, processingOwner)
                                .set(
                                        OrderOutboxEventDO::getStatus,
                                        event.getStatus() == null ? null : event.getStatus().value())
                                .set(OrderOutboxEventDO::getRetryCount, event.getRetryCount())
                                .set(OrderOutboxEventDO::getNextRetryAt, event.getNextRetryAt())
                                .set(OrderOutboxEventDO::getErrorMessage, truncate(event.getErrorMessage()))
                                .set(OrderOutboxEventDO::getProcessingOwner, event.getProcessingOwner())
                                .set(OrderOutboxEventDO::getLeaseUntil, event.getLeaseUntil())
                                .set(OrderOutboxEventDO::getClaimedAt, event.getClaimedAt())
                                .set(OrderOutboxEventDO::getUpdatedAt, event.getUpdatedAt()))
                > 0;
    }

    public boolean markDeadClaimed(
            OutboxId outboxId,
            String processingOwner,
            int retryCount,
            String deadReason,
            String errorMessage,
            Instant updatedAt) {
        OrderOutboxEventDO dataObject = outboxId == null ? null : outboxEventMapper.selectById(outboxId.value());
        if (dataObject == null) {
            return false;
        }
        OrderOutboxEvent event = orderOutboxEventPersistenceAssembler.toDomain(dataObject);
        if (!event.isClaimedBy(processingOwner)) {
            return false;
        }
        event.markDead(processingOwner, deadReason, errorMessage, updatedAt);
        return outboxEventMapper.update(
                        null,
                        Wrappers.<OrderOutboxEventDO>lambdaUpdate()
                                .eq(OrderOutboxEventDO::getId, outboxId == null ? null : outboxId.value())
                                .eq(OrderOutboxEventDO::getStatus, OrderOutboxStatus.PROCESSING.value())
                                .eq(OrderOutboxEventDO::getProcessingOwner, processingOwner)
                                .set(
                                        OrderOutboxEventDO::getStatus,
                                        event.getStatus() == null ? null : event.getStatus().value())
                                .set(OrderOutboxEventDO::getRetryCount, event.getRetryCount())
                                .set(OrderOutboxEventDO::getDeadReason, event.getDeadReason())
                                .set(OrderOutboxEventDO::getErrorMessage, truncate(event.getErrorMessage()))
                                .set(OrderOutboxEventDO::getProcessingOwner, event.getProcessingOwner())
                                .set(OrderOutboxEventDO::getLeaseUntil, event.getLeaseUntil())
                                .set(OrderOutboxEventDO::getClaimedAt, event.getClaimedAt())
                                .set(OrderOutboxEventDO::getUpdatedAt, event.getUpdatedAt()))
                > 0;
    }

    public boolean deleteClaimed(OutboxId outboxId, String processingOwner) {
        return outboxEventMapper.delete(Wrappers.<OrderOutboxEventDO>lambdaQuery()
                        .eq(OrderOutboxEventDO::getId, outboxId == null ? null : outboxId.value())
                        .eq(OrderOutboxEventDO::getStatus, OrderOutboxStatus.PROCESSING.value())
                        .eq(OrderOutboxEventDO::getProcessingOwner, processingOwner))
                > 0;
    }

    public void insert(OrderOutboxDeadLetter deadLetter) {
        OrderOutboxDeadLetterDO dataObject = orderOutboxDeadLetterPersistenceAssembler.toDataObject(deadLetter);
        deadLetterMapper.insert(dataObject);
    }

    public Optional<OrderOutboxDeadLetter> findDeadLetterById(OrderOutboxDeadLetterId id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(deadLetterMapper.selectById(id.value()))
                .map(orderOutboxDeadLetterPersistenceAssembler::toDomain);
    }

    public void markDeadLetterReplaySucceeded(OrderOutboxDeadLetterId id, Instant replayedAt, String message) {
        deadLetterMapper.update(
                null,
                Wrappers.<OrderOutboxDeadLetterDO>lambdaUpdate()
                        .eq(OrderOutboxDeadLetterDO::getId, id == null ? null : id.value())
                        .set(OrderOutboxDeadLetterDO::getReplayStatus, OrderOutboxReplayStatus.SUCCESS.value())
                        .setSql("replay_count = ifnull(replay_count, 0) + 1")
                        .set(OrderOutboxDeadLetterDO::getLastReplayAt, replayedAt)
                        .set(OrderOutboxDeadLetterDO::getLastReplayMessage, truncate(message))
                        .set(OrderOutboxDeadLetterDO::getUpdatedAt, replayedAt));
    }

    public void markDeadLetterReplayFailed(OrderOutboxDeadLetterId id, Instant replayedAt, String message) {
        deadLetterMapper.update(
                null,
                Wrappers.<OrderOutboxDeadLetterDO>lambdaUpdate()
                        .eq(OrderOutboxDeadLetterDO::getId, id == null ? null : id.value())
                        .set(OrderOutboxDeadLetterDO::getReplayStatus, OrderOutboxReplayStatus.FAILED.value())
                        .setSql("replay_count = ifnull(replay_count, 0) + 1")
                        .set(OrderOutboxDeadLetterDO::getLastReplayAt, replayedAt)
                        .set(OrderOutboxDeadLetterDO::getLastReplayMessage, truncate(message))
                        .set(OrderOutboxDeadLetterDO::getUpdatedAt, replayedAt));
    }

    public void markDeadLetterReplayPending(OrderOutboxDeadLetterId id, String message, Instant updatedAt) {
        deadLetterMapper.update(
                null,
                Wrappers.<OrderOutboxDeadLetterDO>lambdaUpdate()
                        .eq(OrderOutboxDeadLetterDO::getId, id == null ? null : id.value())
                        .eq(OrderOutboxDeadLetterDO::getReplayStatus, OrderOutboxReplayStatus.FAILED.value())
                        .set(OrderOutboxDeadLetterDO::getReplayStatus, OrderOutboxReplayStatus.PENDING.value())
                        .set(OrderOutboxDeadLetterDO::getLastReplayMessage, truncate(message))
                        .set(OrderOutboxDeadLetterDO::getUpdatedAt, updatedAt));
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }

    private EventCode generateEventCode() {
        long id = idGenerator.nextId(OUTBOX_EVENT_CODE_BIZ_TAG);
        String timestamp = LocalDateTime.now().format(EVENT_CODE_TIMESTAMP_FORMATTER);
        String suffix = String.format("%06d", Math.floorMod(id, 1_000_000L));
        return EventCode.of("EVT" + timestamp + "-" + suffix);
    }
}
