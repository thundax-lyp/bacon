package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存审计死信记录。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditDeadLetter {

    /** 死信记录主键。 */
    private DeadLetterId id;
    /** 审计出站主键。 */
    private OutboxId outboxId;
    /** 出站事件业务标识。 */
    private EventCode eventCode;
    /** 所属租户主键。 */
    private TenantId tenantId;
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
    /** 最近一次回放结果。 */
    private String lastReplayResult;
    /** 最近一次回放错误。 */
    private String lastReplayError;
    /** 回放幂等键。 */
    private String replayKey;
    /** 回放操作人类型。 */
    private String replayOperatorType;
    /** 回放操作人主键。 */
    private String replayOperatorId;

    public InventoryAuditDeadLetter(Long id, Long outboxId, String eventCode, Long tenantId, String orderNo,
                                    String reservationNo, String actionType, String operatorType, Long operatorId,
                                    Instant occurredAt, Integer retryCount, String errorMessage, String deadReason,
                                    Instant deadAt, String replayStatus, Integer replayCount, Instant lastReplayAt,
                                    String lastReplayResult, String lastReplayError, String replayKey,
                                    String replayOperatorType, String replayOperatorId) {
        this(id == null ? null : DeadLetterId.of(id),
                outboxId == null ? null : OutboxId.of(outboxId),
                eventCode == null ? null : EventCode.of(eventCode),
                tenantId == null ? null : TenantId.of(tenantId),
                orderNo == null ? null : OrderNo.of(orderNo),
                reservationNo == null ? null : ReservationNo.of(reservationNo),
                actionType == null ? null : InventoryAuditActionType.from(actionType),
                operatorType == null ? null : InventoryAuditOperatorType.from(operatorType),
                operatorId == null ? null : String.valueOf(operatorId),
                occurredAt,
                retryCount,
                errorMessage,
                deadReason,
                deadAt,
                replayStatus == null ? null : InventoryAuditReplayStatus.from(replayStatus),
                replayCount,
                lastReplayAt,
                lastReplayResult,
                lastReplayError,
                replayKey,
                replayOperatorType,
                replayOperatorId);
    }

    public Long getIdValue() {
        return id == null ? null : id.value();
    }

    public Long getOutboxIdValue() {
        return outboxId == null ? null : outboxId.value();
    }

    public String getEventCodeValue() {
        return eventCode == null ? null : eventCode.value();
    }

    public String getReplayStatusValue() {
        return replayStatus == null ? null : replayStatus.value();
    }

    public Long getTenantIdValue() {
        return tenantId == null ? null : tenantId.value();
    }

    public String getOrderNoValue() {
        return orderNo == null ? null : orderNo.value();
    }

    public String getReservationNoValue() {
        return reservationNo == null ? null : reservationNo.value();
    }

    public String getActionTypeValue() {
        return actionType == null ? null : actionType.value();
    }

    public String getOperatorTypeValue() {
        return operatorType == null ? null : operatorType.value();
    }

    public Long getOperatorIdValue() {
        return operatorId == null ? null : Long.valueOf(operatorId);
    }

    public Long getReplayOperatorIdValue() {
        return replayOperatorId == null ? null : Long.valueOf(replayOperatorId);
    }

}
