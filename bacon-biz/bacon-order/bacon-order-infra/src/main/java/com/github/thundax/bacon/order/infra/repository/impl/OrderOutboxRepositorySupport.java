package com.github.thundax.bacon.order.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxReplayStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.EventId;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderOutboxDeadLetterDO;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderOutboxEventDO;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderOutboxDeadLetterMapper;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderOutboxEventMapper;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
public class OrderOutboxRepositorySupport {

    private static final String OUTBOX_ID_BIZ_TAG = "order_outbox_id";
    private static final String OUTBOX_EVENT_ID_BIZ_TAG = "order_outbox_event_id";
    private static final String DEAD_LETTER_ID_BIZ_TAG = "order_outbox_dead_letter_id";

    private final OrderOutboxEventMapper outboxEventMapper;
    private final OrderOutboxDeadLetterMapper deadLetterMapper;
    private final IdGenerator idGenerator;

    public OrderOutboxRepositorySupport(OrderOutboxEventMapper outboxEventMapper,
                                        OrderOutboxDeadLetterMapper deadLetterMapper,
                                        IdGenerator idGenerator) {
        this.outboxEventMapper = outboxEventMapper;
        this.deadLetterMapper = deadLetterMapper;
        this.idGenerator = idGenerator;
    }

    public void saveOutboxEvent(OrderOutboxEvent event) {
        OrderOutboxEventDO dataObject = toDataObject(event);
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
        if (dataObject.getEventId() == null || dataObject.getEventId().isBlank()) {
            dataObject.setEventId(generateEventId().value());
        }
        // outbox 插入即进入可调度状态，后续所有处理权转移都通过 status + processingOwner + leaseUntil 控制。
        outboxEventMapper.insert(dataObject);
        event.setId(dataObject.getId());
        event.setEventId(EventId.of(dataObject.getEventId()));
    }

    public List<OrderOutboxEvent> claimRetryableOutbox(Instant now, int limit, String processingOwner, Instant leaseUntil) {
        List<OrderOutboxEventDO> candidates = outboxEventMapper.selectList(Wrappers.<OrderOutboxEventDO>lambdaQuery()
                        .in(OrderOutboxEventDO::getStatus, OrderOutboxStatus.NEW.value(),
                                OrderOutboxStatus.RETRYING.value())
                        .and(wrapper -> wrapper.isNull(OrderOutboxEventDO::getNextRetryAt)
                                .or().le(OrderOutboxEventDO::getNextRetryAt, now))
                        .orderByAsc(OrderOutboxEventDO::getCreatedAt, OrderOutboxEventDO::getId)
                        .last("limit " + Math.max(limit * 3, limit)))
                .stream()
                .toList();
        if (candidates.isEmpty()) {
            return List.of();
        }
        java.util.List<OrderOutboxEvent> claimed = new java.util.ArrayList<>(limit);
        // 先粗查候选，再逐条 CAS 抢占，避免多节点并发扫描时把同一批事件都误认为可处理。
        for (OrderOutboxEventDO candidate : candidates) {
            if (claimed.size() >= limit) {
                break;
            }
            int updated = outboxEventMapper.update(null, Wrappers.<OrderOutboxEventDO>lambdaUpdate()
                    .eq(OrderOutboxEventDO::getId, candidate.getId())
                    .in(OrderOutboxEventDO::getStatus, OrderOutboxStatus.NEW.value(), OrderOutboxStatus.RETRYING.value())
                    .and(wrapper -> wrapper.isNull(OrderOutboxEventDO::getNextRetryAt)
                            .or().le(OrderOutboxEventDO::getNextRetryAt, now))
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
                claimed.add(toDomain(claimedDo));
            }
        }
        return List.copyOf(claimed);
    }

    public int releaseExpiredLease(Instant now) {
        // 租约过期的 PROCESSING 事件统一回到 RETRYING，等待下一轮重试器重新认领。
        return outboxEventMapper.update(null, Wrappers.<OrderOutboxEventDO>lambdaUpdate()
                .eq(OrderOutboxEventDO::getStatus, OrderOutboxStatus.PROCESSING.value())
                .le(OrderOutboxEventDO::getLeaseUntil, now)
                .set(OrderOutboxEventDO::getStatus, OrderOutboxStatus.RETRYING.value())
                .set(OrderOutboxEventDO::getProcessingOwner, null)
                .set(OrderOutboxEventDO::getLeaseUntil, null)
                .set(OrderOutboxEventDO::getClaimedAt, null)
                .set(OrderOutboxEventDO::getUpdatedAt, now));
    }

    public boolean markRetryingClaimed(Long outboxId, String processingOwner, int retryCount,
                                       Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        // 只有当前 owner 仍持有执行权时才允许改回 RETRYING，避免旧节点回写覆盖新节点状态。
        return outboxEventMapper.update(null, Wrappers.<OrderOutboxEventDO>lambdaUpdate()
                .eq(OrderOutboxEventDO::getId, outboxId)
                .eq(OrderOutboxEventDO::getStatus, OrderOutboxStatus.PROCESSING.value())
                .eq(OrderOutboxEventDO::getProcessingOwner, processingOwner)
                .set(OrderOutboxEventDO::getStatus, OrderOutboxStatus.RETRYING.value())
                .set(OrderOutboxEventDO::getRetryCount, retryCount)
                .set(OrderOutboxEventDO::getNextRetryAt, nextRetryAt)
                .set(OrderOutboxEventDO::getErrorMessage, truncate(errorMessage))
                .set(OrderOutboxEventDO::getProcessingOwner, null)
                .set(OrderOutboxEventDO::getLeaseUntil, null)
                .set(OrderOutboxEventDO::getClaimedAt, null)
                .set(OrderOutboxEventDO::getUpdatedAt, updatedAt)) > 0;
    }

    public boolean markDeadClaimed(Long outboxId, String processingOwner, int retryCount,
                                   String deadReason, String errorMessage, Instant updatedAt) {
        // DEAD 也是带 owner 条件的 CAS 更新，确保只有最后一次失败的真实执行者能把事件送进死信。
        return outboxEventMapper.update(null, Wrappers.<OrderOutboxEventDO>lambdaUpdate()
                .eq(OrderOutboxEventDO::getId, outboxId)
                .eq(OrderOutboxEventDO::getStatus, OrderOutboxStatus.PROCESSING.value())
                .eq(OrderOutboxEventDO::getProcessingOwner, processingOwner)
                .set(OrderOutboxEventDO::getStatus, OrderOutboxStatus.DEAD.value())
                .set(OrderOutboxEventDO::getRetryCount, retryCount)
                .set(OrderOutboxEventDO::getDeadReason, deadReason)
                .set(OrderOutboxEventDO::getErrorMessage, truncate(errorMessage))
                .set(OrderOutboxEventDO::getProcessingOwner, null)
                .set(OrderOutboxEventDO::getLeaseUntil, null)
                .set(OrderOutboxEventDO::getClaimedAt, null)
                .set(OrderOutboxEventDO::getUpdatedAt, updatedAt)) > 0;
    }

    public boolean deleteClaimed(Long outboxId, String processingOwner) {
        return outboxEventMapper.delete(Wrappers.<OrderOutboxEventDO>lambdaQuery()
                .eq(OrderOutboxEventDO::getId, outboxId)
                .eq(OrderOutboxEventDO::getStatus, OrderOutboxStatus.PROCESSING.value())
                .eq(OrderOutboxEventDO::getProcessingOwner, processingOwner)) > 0;
    }

    public void saveDeadLetter(OrderOutboxDeadLetter deadLetter) {
        OrderOutboxDeadLetterDO dataObject = toDataObject(deadLetter);
        Instant now = Instant.now();
        dataObject.setCreatedAt(dataObject.getCreatedAt() == null ? now : dataObject.getCreatedAt());
        dataObject.setUpdatedAt(now);
        if (dataObject.getReplayStatus() == null) {
            dataObject.setReplayStatus(OrderOutboxReplayStatus.PENDING.value());
        }
        if (dataObject.getReplayCount() == null) {
            dataObject.setReplayCount(0);
        }
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId(DEAD_LETTER_ID_BIZ_TAG));
        }
        deadLetterMapper.insert(dataObject);
    }

    private String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }

    private OrderOutboxEventDO toDataObject(OrderOutboxEvent event) {
        return new OrderOutboxEventDO(event.getId(), toDatabaseEventId(event.getEventId()), toDatabaseTenantId(event.getTenantId()),
                toDatabaseOrderNo(event.getOrderNo()), toDatabaseEventType(event.getEventType()), event.getBusinessKey(),
                event.getPayload(), toDatabaseStatus(event.getStatus()),
                event.getRetryCount(), event.getNextRetryAt(), event.getProcessingOwner(), event.getLeaseUntil(),
                event.getClaimedAt(), event.getErrorMessage(), event.getDeadReason(), event.getCreatedAt(),
                event.getUpdatedAt());
    }

    private OrderOutboxEvent toDomain(OrderOutboxEventDO dataObject) {
        return new OrderOutboxEvent(dataObject.getId(), toDomainEventId(dataObject.getEventId()),
                toDomainTenantId(dataObject.getTenantId()), toDomainOrderNo(dataObject.getOrderNo()),
                toDomainEventType(dataObject.getEventType()), dataObject.getBusinessKey(), dataObject.getPayload(),
                toDomainStatus(dataObject.getStatus()),
                dataObject.getRetryCount(), dataObject.getNextRetryAt(), dataObject.getProcessingOwner(),
                dataObject.getLeaseUntil(), dataObject.getClaimedAt(), dataObject.getErrorMessage(),
                dataObject.getDeadReason(), dataObject.getCreatedAt(), dataObject.getUpdatedAt());
    }

    private EventId generateEventId() {
        return EventId.of("EVT" + idGenerator.nextId(OUTBOX_EVENT_ID_BIZ_TAG));
    }

    private String toDatabaseEventId(EventId eventId) {
        return eventId == null ? null : eventId.value();
    }

    private EventId toDomainEventId(String eventId) {
        return eventId == null ? null : EventId.of(eventId);
    }

    private String toDatabaseTenantId(TenantId tenantId) {
        return tenantId == null ? null : tenantId.value();
    }

    private TenantId toDomainTenantId(String tenantId) {
        return tenantId == null ? null : TenantId.of(tenantId);
    }

    private String toDatabaseOrderNo(OrderNo orderNo) {
        return orderNo == null ? null : orderNo.value();
    }

    private OrderNo toDomainOrderNo(String orderNo) {
        return orderNo == null ? null : OrderNo.of(orderNo);
    }

    private String toDatabaseEventType(OrderOutboxEventType eventType) {
        return eventType == null ? null : eventType.value();
    }

    private OrderOutboxEventType toDomainEventType(String eventType) {
        return eventType == null ? null : OrderOutboxEventType.fromValue(eventType);
    }

    private String toDatabaseStatus(OrderOutboxStatus status) {
        return status == null ? null : status.value();
    }

    private OrderOutboxStatus toDomainStatus(String status) {
        return status == null ? null : OrderOutboxStatus.fromValue(status);
    }

    private OrderOutboxDeadLetterDO toDataObject(OrderOutboxDeadLetter deadLetter) {
        return new OrderOutboxDeadLetterDO(null, deadLetter.getOutboxId(), deadLetter.getEventIdValue(),
                deadLetter.getTenantIdRawValue(), deadLetter.getOrderNoValue(), deadLetter.getEventTypeValue(),
                deadLetter.getBusinessKey(), deadLetter.getPayload(),
                deadLetter.getRetryCount(), deadLetter.getErrorMessage(), deadLetter.getDeadReason(), deadLetter.getDeadAt(),
                deadLetter.getReplayStatusValue(), deadLetter.getReplayCount(), deadLetter.getLastReplayAt(),
                deadLetter.getLastReplayMessage(), deadLetter.getCreatedAt(), deadLetter.getUpdatedAt());
    }
}
