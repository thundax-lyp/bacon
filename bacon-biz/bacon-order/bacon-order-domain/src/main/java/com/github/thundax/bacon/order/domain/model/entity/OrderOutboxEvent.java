package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.exception.OrderDomainException;
import com.github.thundax.bacon.order.domain.exception.OrderErrorCode;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.order.domain.model.valueobject.OutboxId;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 订单出站事件。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderOutboxEvent {

    private static final int MESSAGE_MAX_LENGTH = 512;

    /** 出站事件主键。 */
    private OutboxId id;
    /** 出站事件业务标识。 */
    private EventCode eventCode;
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
        Instant now = createdAt == null ? Instant.now() : createdAt;
        return new OrderOutboxEvent(
                null,
                null,
                orderNo == null ? null : OrderNo.of(orderNo),
                eventType,
                businessKey,
                payload,
                status == null ? OrderOutboxStatus.NEW : status,
                retryCount == null ? 0 : retryCount,
                nextRetryAt,
                processingOwner,
                leaseUntil,
                claimedAt,
                normalizeMessage(errorMessage),
                deadReason,
                now,
                updatedAt == null ? now : updatedAt);
    }

    public static OrderOutboxEvent reconstruct(
            OutboxId id,
            EventCode eventCode,
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
        Instant resolvedCreatedAt = createdAt == null ? Instant.now() : createdAt;
        return new OrderOutboxEvent(
                id,
                eventCode,
                orderNo,
                eventType,
                businessKey,
                payload,
                status == null ? OrderOutboxStatus.NEW : status,
                retryCount == null ? 0 : retryCount,
                nextRetryAt,
                processingOwner,
                leaseUntil,
                claimedAt,
                normalizeMessage(errorMessage),
                deadReason,
                resolvedCreatedAt,
                updatedAt == null ? resolvedCreatedAt : updatedAt);
    }

    public boolean isClaimable(Instant now) {
        return isNewOrRetrying() && (this.nextRetryAt == null || !this.nextRetryAt.isAfter(now));
    }

    public boolean isClaimedBy(String owner) {
        return this.status == OrderOutboxStatus.PROCESSING && Objects.equals(this.processingOwner, owner);
    }

    public boolean isLeaseExpired(Instant now) {
        return this.leaseUntil != null && !this.leaseUntil.isAfter(now);
    }

    public int nextRetryCount() {
        return (this.retryCount == null ? 0 : this.retryCount) + 1;
    }

    public void claim(String owner, Instant leaseUntil, Instant claimedAt) {
        if (!isNewOrRetrying()) {
            throw new OrderDomainException(
                    OrderErrorCode.INVALID_OUTBOX_EVENT,
                    this.status == null ? null : this.status.value());
        }
        if (this.nextRetryAt != null && claimedAt != null && this.nextRetryAt.isAfter(claimedAt)) {
            throw new OrderDomainException(OrderErrorCode.INVALID_OUTBOX_EVENT, "nextRetryAt");
        }
        this.status = OrderOutboxStatus.PROCESSING;
        this.processingOwner = owner;
        this.leaseUntil = leaseUntil;
        this.claimedAt = claimedAt;
        this.updatedAt = claimedAt == null ? Instant.now() : claimedAt;
    }

    public void releaseExpiredLease(Instant now) {
        if (this.status != OrderOutboxStatus.PROCESSING || !isLeaseExpired(now)) {
            throw new OrderDomainException(
                    OrderErrorCode.INVALID_OUTBOX_EVENT,
                    this.status == null ? null : this.status.value());
        }
        this.status = OrderOutboxStatus.RETRYING;
        this.processingOwner = null;
        this.leaseUntil = null;
        this.claimedAt = null;
        this.updatedAt = now;
    }

    public void markRetrying(String owner, Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        if (!isClaimedBy(owner)) {
            throw new OrderDomainException(OrderErrorCode.INVALID_OUTBOX_EVENT, owner);
        }
        this.status = OrderOutboxStatus.RETRYING;
        this.retryCount = nextRetryCount();
        this.nextRetryAt = nextRetryAt;
        this.errorMessage = normalizeMessage(errorMessage);
        this.processingOwner = null;
        this.leaseUntil = null;
        this.claimedAt = null;
        this.updatedAt = updatedAt == null ? Instant.now() : updatedAt;
    }

    public void markDead(String owner, String deadReason, String errorMessage, Instant updatedAt) {
        if (!isClaimedBy(owner)) {
            throw new OrderDomainException(OrderErrorCode.INVALID_OUTBOX_EVENT, owner);
        }
        this.status = OrderOutboxStatus.DEAD;
        this.retryCount = nextRetryCount();
        this.deadReason = deadReason;
        this.errorMessage = normalizeMessage(errorMessage);
        this.processingOwner = null;
        this.leaseUntil = null;
        this.claimedAt = null;
        this.updatedAt = updatedAt == null ? Instant.now() : updatedAt;
    }

    private boolean isNewOrRetrying() {
        return this.status == OrderOutboxStatus.NEW || this.status == OrderOutboxStatus.RETRYING;
    }

    private static String normalizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        return message.length() <= MESSAGE_MAX_LENGTH ? message : message.substring(0, MESSAGE_MAX_LENGTH);
    }
}
