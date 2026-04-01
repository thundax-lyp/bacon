package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderOutboxDeadLetterDataObject;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderOutboxEventDataObject;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderOutboxDeadLetterMapper;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderOutboxEventMapper;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderOutboxRepositorySupport {

    private final OrderOutboxEventMapper outboxEventMapper;
    private final OrderOutboxDeadLetterMapper deadLetterMapper;

    public OrderOutboxRepositorySupport(OrderOutboxEventMapper outboxEventMapper,
                                        OrderOutboxDeadLetterMapper deadLetterMapper) {
        this.outboxEventMapper = outboxEventMapper;
        this.deadLetterMapper = deadLetterMapper;
    }

    public void saveOutboxEvent(OrderOutboxEvent event) {
        OrderOutboxEventDataObject dataObject = toDataObject(event);
        Instant now = Instant.now();
        dataObject.setCreatedAt(dataObject.getCreatedAt() == null ? now : dataObject.getCreatedAt());
        dataObject.setUpdatedAt(now);
        if (dataObject.getStatus() == null) {
            dataObject.setStatus(OrderOutboxEvent.STATUS_NEW);
        }
        if (dataObject.getRetryCount() == null) {
            dataObject.setRetryCount(0);
        }
        // outbox 插入即进入可调度状态，后续所有处理权转移都通过 status + processingOwner + leaseUntil 控制。
        outboxEventMapper.insert(dataObject);
        event.setId(dataObject.getId());
    }

    public List<OrderOutboxEvent> claimRetryableOutbox(Instant now, int limit, String processingOwner, Instant leaseUntil) {
        List<OrderOutboxEventDataObject> candidates = outboxEventMapper.selectList(Wrappers.<OrderOutboxEventDataObject>lambdaQuery()
                        .in(OrderOutboxEventDataObject::getStatus, OrderOutboxEvent.STATUS_NEW,
                                OrderOutboxEvent.STATUS_RETRYING)
                        .and(wrapper -> wrapper.isNull(OrderOutboxEventDataObject::getNextRetryAt)
                                .or().le(OrderOutboxEventDataObject::getNextRetryAt, now))
                        .orderByAsc(OrderOutboxEventDataObject::getCreatedAt, OrderOutboxEventDataObject::getId)
                        .last("limit " + Math.max(limit * 3, limit)))
                .stream()
                .toList();
        if (candidates.isEmpty()) {
            return List.of();
        }
        java.util.List<OrderOutboxEvent> claimed = new java.util.ArrayList<>(limit);
        // 先粗查候选，再逐条 CAS 抢占，避免多节点并发扫描时把同一批事件都误认为可处理。
        for (OrderOutboxEventDataObject candidate : candidates) {
            if (claimed.size() >= limit) {
                break;
            }
            int updated = outboxEventMapper.update(null, Wrappers.<OrderOutboxEventDataObject>lambdaUpdate()
                    .eq(OrderOutboxEventDataObject::getId, candidate.getId())
                    .in(OrderOutboxEventDataObject::getStatus, OrderOutboxEvent.STATUS_NEW, OrderOutboxEvent.STATUS_RETRYING)
                    .and(wrapper -> wrapper.isNull(OrderOutboxEventDataObject::getNextRetryAt)
                            .or().le(OrderOutboxEventDataObject::getNextRetryAt, now))
                    .set(OrderOutboxEventDataObject::getStatus, OrderOutboxEvent.STATUS_PROCESSING)
                    .set(OrderOutboxEventDataObject::getProcessingOwner, processingOwner)
                    .set(OrderOutboxEventDataObject::getLeaseUntil, leaseUntil)
                    .set(OrderOutboxEventDataObject::getClaimedAt, now)
                    .set(OrderOutboxEventDataObject::getUpdatedAt, now));
            if (updated == 0) {
                continue;
            }
            OrderOutboxEventDataObject claimedDo = outboxEventMapper.selectById(candidate.getId());
            if (claimedDo != null) {
                claimed.add(toDomain(claimedDo));
            }
        }
        return List.copyOf(claimed);
    }

    public int releaseExpiredLease(Instant now) {
        // 租约过期的 PROCESSING 事件统一回到 RETRYING，等待下一轮重试器重新认领。
        return outboxEventMapper.update(null, Wrappers.<OrderOutboxEventDataObject>lambdaUpdate()
                .eq(OrderOutboxEventDataObject::getStatus, OrderOutboxEvent.STATUS_PROCESSING)
                .le(OrderOutboxEventDataObject::getLeaseUntil, now)
                .set(OrderOutboxEventDataObject::getStatus, OrderOutboxEvent.STATUS_RETRYING)
                .set(OrderOutboxEventDataObject::getProcessingOwner, null)
                .set(OrderOutboxEventDataObject::getLeaseUntil, null)
                .set(OrderOutboxEventDataObject::getClaimedAt, null)
                .set(OrderOutboxEventDataObject::getUpdatedAt, now));
    }

    public boolean markRetryingClaimed(Long outboxId, String processingOwner, int retryCount,
                                       Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        // 只有当前 owner 仍持有执行权时才允许改回 RETRYING，避免旧节点回写覆盖新节点状态。
        return outboxEventMapper.update(null, Wrappers.<OrderOutboxEventDataObject>lambdaUpdate()
                .eq(OrderOutboxEventDataObject::getId, outboxId)
                .eq(OrderOutboxEventDataObject::getStatus, OrderOutboxEvent.STATUS_PROCESSING)
                .eq(OrderOutboxEventDataObject::getProcessingOwner, processingOwner)
                .set(OrderOutboxEventDataObject::getStatus, OrderOutboxEvent.STATUS_RETRYING)
                .set(OrderOutboxEventDataObject::getRetryCount, retryCount)
                .set(OrderOutboxEventDataObject::getNextRetryAt, nextRetryAt)
                .set(OrderOutboxEventDataObject::getErrorMessage, truncate(errorMessage))
                .set(OrderOutboxEventDataObject::getProcessingOwner, null)
                .set(OrderOutboxEventDataObject::getLeaseUntil, null)
                .set(OrderOutboxEventDataObject::getClaimedAt, null)
                .set(OrderOutboxEventDataObject::getUpdatedAt, updatedAt)) > 0;
    }

    public boolean markDeadClaimed(Long outboxId, String processingOwner, int retryCount,
                                   String deadReason, String errorMessage, Instant updatedAt) {
        // DEAD 也是带 owner 条件的 CAS 更新，确保只有最后一次失败的真实执行者能把事件送进死信。
        return outboxEventMapper.update(null, Wrappers.<OrderOutboxEventDataObject>lambdaUpdate()
                .eq(OrderOutboxEventDataObject::getId, outboxId)
                .eq(OrderOutboxEventDataObject::getStatus, OrderOutboxEvent.STATUS_PROCESSING)
                .eq(OrderOutboxEventDataObject::getProcessingOwner, processingOwner)
                .set(OrderOutboxEventDataObject::getStatus, OrderOutboxEvent.STATUS_DEAD)
                .set(OrderOutboxEventDataObject::getRetryCount, retryCount)
                .set(OrderOutboxEventDataObject::getDeadReason, deadReason)
                .set(OrderOutboxEventDataObject::getErrorMessage, truncate(errorMessage))
                .set(OrderOutboxEventDataObject::getProcessingOwner, null)
                .set(OrderOutboxEventDataObject::getLeaseUntil, null)
                .set(OrderOutboxEventDataObject::getClaimedAt, null)
                .set(OrderOutboxEventDataObject::getUpdatedAt, updatedAt)) > 0;
    }

    public boolean deleteClaimed(Long outboxId, String processingOwner) {
        return outboxEventMapper.delete(Wrappers.<OrderOutboxEventDataObject>lambdaQuery()
                .eq(OrderOutboxEventDataObject::getId, outboxId)
                .eq(OrderOutboxEventDataObject::getStatus, OrderOutboxEvent.STATUS_PROCESSING)
                .eq(OrderOutboxEventDataObject::getProcessingOwner, processingOwner)) > 0;
    }

    public void saveDeadLetter(OrderOutboxDeadLetter deadLetter) {
        OrderOutboxDeadLetterDataObject dataObject = toDataObject(deadLetter);
        Instant now = Instant.now();
        dataObject.setCreatedAt(dataObject.getCreatedAt() == null ? now : dataObject.getCreatedAt());
        dataObject.setUpdatedAt(now);
        if (dataObject.getReplayStatus() == null) {
            dataObject.setReplayStatus(OrderOutboxDeadLetter.REPLAY_STATUS_PENDING);
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

    private OrderOutboxEventDataObject toDataObject(OrderOutboxEvent event) {
        return new OrderOutboxEventDataObject(event.getId(), event.getTenantId(), event.getOrderNo(),
                event.getEventType(), event.getBusinessKey(), event.getPayload(), event.getStatus(),
                event.getRetryCount(), event.getNextRetryAt(), event.getProcessingOwner(), event.getLeaseUntil(),
                event.getClaimedAt(), event.getErrorMessage(), event.getDeadReason(), event.getCreatedAt(),
                event.getUpdatedAt());
    }

    private OrderOutboxEvent toDomain(OrderOutboxEventDataObject dataObject) {
        return new OrderOutboxEvent(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderNo(),
                dataObject.getEventType(), dataObject.getBusinessKey(), dataObject.getPayload(), dataObject.getStatus(),
                dataObject.getRetryCount(), dataObject.getNextRetryAt(), dataObject.getProcessingOwner(),
                dataObject.getLeaseUntil(), dataObject.getClaimedAt(), dataObject.getErrorMessage(),
                dataObject.getDeadReason(), dataObject.getCreatedAt(), dataObject.getUpdatedAt());
    }

    private OrderOutboxDeadLetterDataObject toDataObject(OrderOutboxDeadLetter deadLetter) {
        return new OrderOutboxDeadLetterDataObject(deadLetter.getId(), deadLetter.getOutboxId(), deadLetter.getTenantId(),
                deadLetter.getOrderNo(), deadLetter.getEventType(), deadLetter.getBusinessKey(), deadLetter.getPayload(),
                deadLetter.getRetryCount(), deadLetter.getErrorMessage(), deadLetter.getDeadReason(), deadLetter.getDeadAt(),
                deadLetter.getReplayStatus(), deadLetter.getReplayCount(), deadLetter.getLastReplayAt(),
                deadLetter.getLastReplayMessage(), deadLetter.getCreatedAt(), deadLetter.getUpdatedAt());
    }
}
