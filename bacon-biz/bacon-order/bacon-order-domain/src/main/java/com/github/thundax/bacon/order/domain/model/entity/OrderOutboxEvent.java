package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.order.domain.model.valueobject.OutboxId;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 订单出站事件。
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderOutboxEvent {
    /** 出站事件主键。 */
    private OutboxId id;
    /** 出站事件业务标识。 */
    private EventCode eventCode;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 订单号。 */
    private OrderNo orderNo;
    /** 事件类型。 */
    private OrderOutboxEventType eventType;
    /** 业务幂等键。 */
    private String businessKey;
    /** 事件载荷。 */
    private String payload;
    /** 当前状态。 */
    private OrderOutboxStatus status;
    /** 重试次数。 */
    private Integer retryCount;
    /** 下次重试时间。 */
    private Instant nextRetryAt;
    /** 当前处理节点标识。 */
    private String processingOwner;
    /** 租约到期时间。 */
    private Instant leaseUntil;
    /** 领取处理时间。 */
    private Instant claimedAt;
    /** 错误信息。 */
    private String errorMessage;
    /** 死信原因。 */
    private String deadReason;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public static OrderOutboxEvent create(
            Long tenantId,
            String orderNo,
            OrderOutboxEventType eventType,
            String businessKey,
            String payload,
            OrderOutboxStatus status,
            Integer retryCount,
            Instant nextRetryAt,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            String errorMessage,
            String deadReason,
            Instant createdAt,
            Instant updatedAt) {
        return new OrderOutboxEvent(
                null,
                null,
                tenantId == null ? null : TenantId.of(tenantId),
                orderNo == null ? null : OrderNo.of(orderNo),
                eventType,
                businessKey,
                payload,
                status,
                retryCount,
                nextRetryAt,
                processingOwner,
                leaseUntil,
                claimedAt,
                errorMessage,
                deadReason,
                createdAt,
                updatedAt);
    }

    public static OrderOutboxEvent reconstruct(
            OutboxId id,
            EventCode eventCode,
            TenantId tenantId,
            OrderNo orderNo,
            OrderOutboxEventType eventType,
            String businessKey,
            String payload,
            OrderOutboxStatus status,
            Integer retryCount,
            Instant nextRetryAt,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            String errorMessage,
            String deadReason,
            Instant createdAt,
            Instant updatedAt) {
        return new OrderOutboxEvent(
                id,
                eventCode,
                tenantId,
                orderNo,
                eventType,
                businessKey,
                payload,
                status,
                retryCount,
                nextRetryAt,
                processingOwner,
                leaseUntil,
                claimedAt,
                errorMessage,
                deadReason,
                createdAt,
                updatedAt);
    }
}
