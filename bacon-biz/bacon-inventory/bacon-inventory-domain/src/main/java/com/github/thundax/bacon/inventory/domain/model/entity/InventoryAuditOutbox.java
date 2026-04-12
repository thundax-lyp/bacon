package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOutboxStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 库存审计出站事件。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryAuditOutbox {

    /** 出站事件主键。 */
    private OutboxId id;
    /** 出站事件业务标识。 */
    private EventCode eventCode;
    /** 订单号。 */
    private OrderNo orderNo;
    /** 预占单号。 */
    private ReservationNo reservationNo;
    /** 操作类型。 */
    private InventoryAuditActionType actionType;
    /** 操作人类型。 */
    private InventoryAuditOperatorType operatorType;
    /** 操作人主键。 */
    private String operatorId;
    /** 发生时间。 */
    private Instant occurredAt;
    /** 错误信息。 */
    private String errorMessage;
    /** 当前状态。 */
    private InventoryAuditOutboxStatus status;
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
    /** 死信原因。 */
    private String deadReason;
    /** 失败时间。 */
    private Instant failedAt;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public static InventoryAuditOutbox create(
            OutboxId id,
            EventCode eventCode,
            OrderNo orderNo,
            ReservationNo reservationNo,
            InventoryAuditActionType actionType,
            InventoryAuditOperatorType operatorType,
            String operatorId,
            Instant occurredAt,
            String errorMessage,
            InventoryAuditOutboxStatus status,
            Integer retryCount,
            Instant nextRetryAt,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            String deadReason,
            Instant failedAt,
            Instant updatedAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(orderNo, "orderNo must not be null");
        Objects.requireNonNull(reservationNo, "reservationNo must not be null");
        Objects.requireNonNull(actionType, "actionType must not be null");
        Objects.requireNonNull(operatorType, "operatorType must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(retryCount, "retryCount must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        return new InventoryAuditOutbox(
                id,
                eventCode,
                orderNo,
                reservationNo,
                actionType,
                operatorType,
                operatorId,
                occurredAt,
                errorMessage,
                status,
                retryCount,
                nextRetryAt,
                processingOwner,
                leaseUntil,
                claimedAt,
                deadReason,
                failedAt,
                updatedAt);
    }

    public static InventoryAuditOutbox reconstruct(
            OutboxId id,
            EventCode eventCode,
            OrderNo orderNo,
            ReservationNo reservationNo,
            InventoryAuditActionType actionType,
            InventoryAuditOperatorType operatorType,
            String operatorId,
            Instant occurredAt,
            String errorMessage,
            InventoryAuditOutboxStatus status,
            Integer retryCount,
            Instant nextRetryAt,
            String processingOwner,
            Instant leaseUntil,
            Instant claimedAt,
            String deadReason,
            Instant failedAt,
            Instant updatedAt) {
        return new InventoryAuditOutbox(
                id,
                eventCode,
                orderNo,
                reservationNo,
                actionType,
                operatorType,
                operatorId,
                occurredAt,
                errorMessage,
                status,
                retryCount,
                nextRetryAt,
                processingOwner,
                leaseUntil,
                claimedAt,
                deadReason,
                failedAt,
                updatedAt);
    }

    public void assignEventCode(EventCode eventCode) {
        this.eventCode = eventCode;
    }

    public void claim(String processingOwner, Instant leaseUntil, Instant now) {
        this.status = InventoryAuditOutboxStatus.PROCESSING;
        this.processingOwner = processingOwner;
        this.leaseUntil = leaseUntil;
        this.claimedAt = now;
        this.updatedAt = now;
    }

    public void releaseLeaseToRetrying(Instant updatedAt) {
        this.status = InventoryAuditOutboxStatus.RETRYING;
        clearProcessingState();
        this.updatedAt = updatedAt;
    }

    public void markRetrying(int retryCount, Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        this.status = InventoryAuditOutboxStatus.RETRYING;
        this.retryCount = retryCount;
        this.nextRetryAt = nextRetryAt;
        this.errorMessage = errorMessage;
        this.updatedAt = updatedAt;
    }

    public void markRetryingClaimed(int retryCount, Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        markRetrying(retryCount, nextRetryAt, errorMessage, updatedAt);
        clearProcessingState();
    }

    public void markDead(int retryCount, String deadReason, Instant updatedAt) {
        this.status = InventoryAuditOutboxStatus.DEAD;
        this.retryCount = retryCount;
        this.deadReason = deadReason;
        this.updatedAt = updatedAt;
    }

    public void markDeadClaimed(int retryCount, String deadReason, Instant updatedAt) {
        markDead(retryCount, deadReason, updatedAt);
        clearProcessingState();
    }

    private void clearProcessingState() {
        this.processingOwner = null;
        this.leaseUntil = null;
        this.claimedAt = null;
    }
}
