package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
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
 * 库存审计死信记录。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryAuditDeadLetter {

    /** 死信记录主键。 */
    private DeadLetterId id;
    /** 审计出站主键。 */
    private OutboxId outboxId;
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
    /** 原始发生时间。 */
    private Instant occurredAt;
    /** 重试次数。 */
    private Integer retryCount;
    /** 错误信息。 */
    private String errorMessage;
    /** 死信原因。 */
    private String deadReason;
    /** 死信时间。 */
    private Instant deadAt;
    /** 回放状态。 */
    private InventoryAuditReplayStatus replayStatus;
    /** 回放次数。 */
    private Integer replayCount;
    /** 最近一次回放时间。 */
    private Instant lastReplayAt;
    /** 最近一次回放说明。 */
    private String lastReplayMessage;
    /** 最近一次回放错误。 */
    private String lastReplayError;
    /** 回放幂等键。 */
    private String replayKey;
    /** 回放操作人类型。 */
    private String replayOperatorType;
    /** 回放操作人主键。 */
    private String replayOperatorId;

    /**
     * 出站重试耗尽后，创建一条死信。
     */
    public static InventoryAuditDeadLetter create(
            DeadLetterId id,
            OutboxId outboxId,
            EventCode eventCode,
            OrderNo orderNo,
            ReservationNo reservationNo,
            InventoryAuditActionType actionType,
            InventoryAuditOperatorType operatorType,
            String operatorId,
            Instant occurredAt,
            Integer retryCount,
            String errorMessage,
            String deadReason,
            Instant deadAt) {
        Objects.requireNonNull(id, "id must not be null");
        return new InventoryAuditDeadLetter(
                id,
                outboxId,
                eventCode,
                orderNo,
                reservationNo,
                actionType,
                operatorType,
                operatorId,
                occurredAt,
                retryCount,
                errorMessage,
                deadReason,
                deadAt,
                InventoryAuditReplayStatus.PENDING,
                0,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    public static InventoryAuditDeadLetter reconstruct(
            DeadLetterId id,
            OutboxId outboxId,
            EventCode eventCode,
            OrderNo orderNo,
            ReservationNo reservationNo,
            InventoryAuditActionType actionType,
            InventoryAuditOperatorType operatorType,
            String operatorId,
            Instant occurredAt,
            Integer retryCount,
            String errorMessage,
            String deadReason,
            Instant deadAt,
            InventoryAuditReplayStatus replayStatus,
            Integer replayCount,
            Instant lastReplayAt,
            String lastReplayMessage,
            String lastReplayError,
            String replayKey,
            String replayOperatorType,
            String replayOperatorId) {
        return new InventoryAuditDeadLetter(
                id,
                outboxId,
                eventCode,
                orderNo,
                reservationNo,
                actionType,
                operatorType,
                operatorId,
                occurredAt,
                retryCount,
                errorMessage,
                deadReason,
                deadAt,
                replayStatus,
                replayCount,
                lastReplayAt,
                lastReplayMessage,
                lastReplayError,
                replayKey,
                replayOperatorType,
                replayOperatorId);
    }

    public void markReplayRunning(
            String replayKey,
            InventoryAuditOperatorType operatorType,
            String operatorId,
            Instant replayAt) {
        this.replayStatus = InventoryAuditReplayStatus.RUNNING;
        this.replayKey = replayKey;
        this.replayOperatorType = operatorType == null ? null : operatorType.value();
        this.replayOperatorId = operatorId;
        this.lastReplayAt = replayAt;
        this.lastReplayMessage = "RUNNING";
        this.lastReplayError = null;
    }

    public void markReplaySucceeded(
            String replayKey,
            InventoryAuditOperatorType operatorType,
            String operatorId,
            Instant replayAt) {
        this.replayStatus = InventoryAuditReplayStatus.SUCCEEDED;
        this.replayCount = (replayCount == null ? 0 : replayCount) + 1;
        this.replayKey = replayKey;
        this.replayOperatorType = operatorType == null ? null : operatorType.value();
        this.replayOperatorId = operatorId;
        this.lastReplayAt = replayAt;
        this.lastReplayMessage = "SUCCEEDED";
        this.lastReplayError = null;
    }

    public void markReplayFailed(
            String replayKey,
            InventoryAuditOperatorType operatorType,
            String operatorId,
            String replayError,
            Instant replayAt) {
        this.replayStatus = InventoryAuditReplayStatus.FAILED;
        this.replayCount = (replayCount == null ? 0 : replayCount) + 1;
        this.replayKey = replayKey;
        this.replayOperatorType = operatorType == null ? null : operatorType.value();
        this.replayOperatorId = operatorId;
        this.lastReplayAt = replayAt;
        this.lastReplayMessage = "FAILED";
        this.lastReplayError = replayError;
    }
}
