package com.github.thundax.bacon.order.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxReplayStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.order.domain.model.valueobject.OutboxId;
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

    public void saveOutboxEvent(OrderOutboxEvent event) {
        OrderOutboxEventDO dataObject = orderOutboxEventPersistenceAssembler.toDataObject(event);
        Instant now = Instant.now();
        dataObject.setCreatedAt(dataObject.getCreatedAt() == null ? now : dataObject.getCreatedAt());
        dataObject.setUpdatedAt(now);
        if (dataObject.getStatus() == null) {
            dataObject.setStatus(OrderOutboxStatus.NEW.value());
        }
        if (dataObject.getRetryCount() == null) {
            dataObject.setRetryCount(0);
        }
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId(OUTBOX_ID_BIZ_TAG));
        }
        if (dataObject.getEventCode() == null || dataObject.getEventCode().isBlank()) {
            dataObject.setEventCode(generateEventCode().value());
        }
        // outbox 插入即进入可调度状态，后续所有处理权转移都通过 status + processingOwner + leaseUntil 控制。
        outboxEventMapper.insert(dataObject);
        event.setId(dataObject.getId() == null ? null : OutboxId.of(dataObject.getId()));
        event.setEventCode(EventCode.of(dataObject.getEventCode()));
    }

    public List<OrderOutboxEvent> claimRetryableOutbox(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
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
                            .set(OrderOutboxEventDO::getStatus, OrderOutboxStatus.PROCESSING.value())
                            .set(OrderOutboxEventDO::getProcessingOwner, processingOwner)
                            .set(OrderOutboxEventDO::getLeaseUntil, leaseUntil)
                            .set(OrderOutboxEventDO::getClaimedAt, now)
                            .set(OrderOutboxEventDO::getUpdatedAt, now));
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

    public int releaseExpiredLease(Instant now) {
        // 租约过期的 PROCESSING 事件统一回到 RETRYING，等待下一轮重试器重新认领。
        return outboxEventMapper.update(
                null,
                Wrappers.<OrderOutboxEventDO>lambdaUpdate()
                        .eq(OrderOutboxEventDO::getStatus, OrderOutboxStatus.PROCESSING.value())
                        .le(OrderOutboxEventDO::getLeaseUntil, now)
                        .set(OrderOutboxEventDO::getStatus, OrderOutboxStatus.RETRYING.value())
                        .set(OrderOutboxEventDO::getProcessingOwner, null)
                        .set(OrderOutboxEventDO::getLeaseUntil, null)
                        .set(OrderOutboxEventDO::getClaimedAt, null)
                        .set(OrderOutboxEventDO::getUpdatedAt, now));
    }

    public boolean markRetryingClaimed(
            OutboxId outboxId,
            String processingOwner,
            int retryCount,
            Instant nextRetryAt,
            String errorMessage,
            Instant updatedAt) {
        // 只有当前 owner 仍持有执行权时才允许改回 RETRYING，避免旧节点回写覆盖新节点状态。
        return outboxEventMapper.update(
                        null,
                        Wrappers.<OrderOutboxEventDO>lambdaUpdate()
                                .eq(OrderOutboxEventDO::getId, outboxId == null ? null : outboxId.value())
                                .eq(OrderOutboxEventDO::getStatus, OrderOutboxStatus.PROCESSING.value())
                                .eq(OrderOutboxEventDO::getProcessingOwner, processingOwner)
                                .set(OrderOutboxEventDO::getStatus, OrderOutboxStatus.RETRYING.value())
                                .set(OrderOutboxEventDO::getRetryCount, retryCount)
                                .set(OrderOutboxEventDO::getNextRetryAt, nextRetryAt)
                                .set(OrderOutboxEventDO::getErrorMessage, truncate(errorMessage))
                                .set(OrderOutboxEventDO::getProcessingOwner, null)
                                .set(OrderOutboxEventDO::getLeaseUntil, null)
                                .set(OrderOutboxEventDO::getClaimedAt, null)
                                .set(OrderOutboxEventDO::getUpdatedAt, updatedAt))
                > 0;
    }

    public boolean markDeadClaimed(
            OutboxId outboxId,
            String processingOwner,
            int retryCount,
            String deadReason,
            String errorMessage,
            Instant updatedAt) {
        // DEAD 也是带 owner 条件的 CAS 更新，确保只有最后一次失败的真实执行者能把事件送进死信。
        return outboxEventMapper.update(
                        null,
                        Wrappers.<OrderOutboxEventDO>lambdaUpdate()
                                .eq(OrderOutboxEventDO::getId, outboxId == null ? null : outboxId.value())
                                .eq(OrderOutboxEventDO::getStatus, OrderOutboxStatus.PROCESSING.value())
                                .eq(OrderOutboxEventDO::getProcessingOwner, processingOwner)
                                .set(OrderOutboxEventDO::getStatus, OrderOutboxStatus.DEAD.value())
                                .set(OrderOutboxEventDO::getRetryCount, retryCount)
                                .set(OrderOutboxEventDO::getDeadReason, deadReason)
                                .set(OrderOutboxEventDO::getErrorMessage, truncate(errorMessage))
                                .set(OrderOutboxEventDO::getProcessingOwner, null)
                                .set(OrderOutboxEventDO::getLeaseUntil, null)
                                .set(OrderOutboxEventDO::getClaimedAt, null)
                                .set(OrderOutboxEventDO::getUpdatedAt, updatedAt))
                > 0;
    }

    public boolean deleteClaimed(OutboxId outboxId, String processingOwner) {
        return outboxEventMapper.delete(Wrappers.<OrderOutboxEventDO>lambdaQuery()
                        .eq(OrderOutboxEventDO::getId, outboxId == null ? null : outboxId.value())
                        .eq(OrderOutboxEventDO::getStatus, OrderOutboxStatus.PROCESSING.value())
                        .eq(OrderOutboxEventDO::getProcessingOwner, processingOwner))
                > 0;
    }

    public void saveDeadLetter(OrderOutboxDeadLetter deadLetter) {
        OrderOutboxDeadLetterDO dataObject = orderOutboxDeadLetterPersistenceAssembler.toDataObject(deadLetter);
        Instant now = Instant.now();
        dataObject.setCreatedAt(dataObject.getCreatedAt() == null ? now : dataObject.getCreatedAt());
        dataObject.setUpdatedAt(now);
        if (dataObject.getReplayStatus() == null) {
            dataObject.setReplayStatus(OrderOutboxReplayStatus.PENDING.value());
        }
        if (dataObject.getReplayCount() == null) {
            dataObject.setReplayCount(0);
        }
        deadLetterMapper.insert(dataObject);
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

    private Long requireTenantId() {
        return BaconContextHolder.requireTenantId();
    }
}
