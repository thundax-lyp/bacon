package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存审计死信记录。
 */
@Data
@NoArgsConstructor
public class InventoryAuditDeadLetter {

    public static final String REPLAY_STATUS_PENDING = "PENDING";
    public static final String REPLAY_STATUS_RUNNING = "RUNNING";
    public static final String REPLAY_STATUS_SUCCEEDED = "SUCCEEDED";
    public static final String REPLAY_STATUS_FAILED = "FAILED";

    /** 死信记录主键。 */
    private Long id;
    /** 审计出站主键。 */
    private Long outboxId;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 订单号。 */
    private OrderNo orderNo;
    /** 预占单号。 */
    private String reservationNo;
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
    private String replayStatus;
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

    public InventoryAuditDeadLetter(Long outboxId, Long tenantId, String orderNo, String reservationNo,
                                    String actionType, String operatorType, Long operatorId, Instant occurredAt,
                                    Integer retryCount, String errorMessage, String deadReason, Instant deadAt) {
        this(null, outboxId, toTenantId(tenantId), toOrderNo(orderNo), reservationNo, toActionType(actionType),
                toOperatorType(operatorType), toStringValue(operatorId), occurredAt, retryCount, errorMessage,
                deadReason, deadAt, REPLAY_STATUS_PENDING, 0, null, null, null, null, null, null);
    }

    public InventoryAuditDeadLetter(Long outboxId, Long tenantId, String orderNo, String reservationNo,
                                    String actionType, String operatorType, Long operatorId, Instant occurredAt,
                                    Integer retryCount, String errorMessage, String deadReason, Instant deadAt,
                                    String replayStatus, Integer replayCount, Instant lastReplayAt,
                                    String lastReplayResult, String lastReplayError, String replayKey,
                                    String replayOperatorType, Long replayOperatorId) {
        this(null, outboxId, toTenantId(tenantId), toOrderNo(orderNo), reservationNo, toActionType(actionType),
                toOperatorType(operatorType), toStringValue(operatorId), occurredAt, retryCount, errorMessage,
                deadReason, deadAt, replayStatus, replayCount, lastReplayAt, lastReplayResult, lastReplayError,
                replayKey, replayOperatorType, toStringValue(replayOperatorId));
    }

    public InventoryAuditDeadLetter(Long id, Long outboxId, Long tenantId, String orderNo, String reservationNo,
                                    String actionType, String operatorType, Long operatorId, Instant occurredAt,
                                    Integer retryCount, String errorMessage, String deadReason, Instant deadAt,
                                    String replayStatus, Integer replayCount, Instant lastReplayAt,
                                    String lastReplayResult, String lastReplayError, String replayKey,
                                    String replayOperatorType, Long replayOperatorId) {
        this(id, outboxId, toTenantId(tenantId), toOrderNo(orderNo), reservationNo, toActionType(actionType),
                toOperatorType(operatorType), toStringValue(operatorId), occurredAt, retryCount, errorMessage,
                deadReason, deadAt, replayStatus, replayCount, lastReplayAt, lastReplayResult, lastReplayError,
                replayKey, replayOperatorType, toStringValue(replayOperatorId));
    }

    public InventoryAuditDeadLetter(Long id, Long outboxId, TenantId tenantId, OrderNo orderNo, String reservationNo,
                                    InventoryAuditActionType actionType, InventoryAuditOperatorType operatorType,
                                    String operatorId, Instant occurredAt,
                                    Integer retryCount, String errorMessage, String deadReason, Instant deadAt,
                                    String replayStatus, Integer replayCount, Instant lastReplayAt,
                                    String lastReplayResult, String lastReplayError, String replayKey,
                                    String replayOperatorType, String replayOperatorId) {
        this.id = id;
        this.outboxId = outboxId;
        this.tenantId = tenantId;
        this.orderNo = orderNo;
        this.reservationNo = reservationNo;
        this.actionType = actionType;
        this.operatorType = operatorType;
        this.operatorId = operatorId;
        this.occurredAt = occurredAt;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
        this.deadReason = deadReason;
        this.deadAt = deadAt;
        this.replayStatus = replayStatus;
        this.replayCount = replayCount;
        this.lastReplayAt = lastReplayAt;
        this.lastReplayResult = lastReplayResult;
        this.lastReplayError = lastReplayError;
        this.replayKey = replayKey;
        this.replayOperatorType = replayOperatorType;
        this.replayOperatorId = replayOperatorId;
    }

    public InventoryAuditDeadLetter(Long outboxId, TenantId tenantId, OrderNo orderNo, String reservationNo,
                                    InventoryAuditActionType actionType, InventoryAuditOperatorType operatorType,
                                    String operatorId, Instant occurredAt,
                                    Integer retryCount, String errorMessage, String deadReason, Instant deadAt) {
        this(null, outboxId, tenantId, orderNo, reservationNo, actionType, operatorType, operatorId, occurredAt,
                retryCount, errorMessage, deadReason, deadAt, REPLAY_STATUS_PENDING, 0, null, null, null, null,
                null, null);
    }

    public Long getTenantIdValue() {
        return tenantId == null ? null : tenantId.value();
    }

    public String getOrderNoValue() {
        return orderNo == null ? null : orderNo.value();
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

    private static TenantId toTenantId(Long tenantId) {
        return tenantId == null ? null : TenantId.of(tenantId);
    }

    private static OrderNo toOrderNo(String orderNo) {
        return orderNo == null ? null : OrderNo.of(orderNo);
    }

    private static InventoryAuditActionType toActionType(String actionType) {
        return actionType == null ? null : InventoryAuditActionType.fromValue(actionType);
    }

    private static InventoryAuditOperatorType toOperatorType(String operatorType) {
        return operatorType == null ? null : InventoryAuditOperatorType.fromValue(operatorType);
    }

    private static String toStringValue(Long value) {
        return value == null ? null : String.valueOf(value);
    }
}
