package com.github.thundax.bacon.inventory.domain.model.entity;

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

    public static final String REPLAY_STATUS_PENDING = "PENDING";
    public static final String REPLAY_STATUS_RUNNING = "RUNNING";
    public static final String REPLAY_STATUS_SUCCEEDED = "SUCCEEDED";
    public static final String REPLAY_STATUS_FAILED = "FAILED";

    /** 死信记录主键。 */
    private Long id;
    /** 审计出站主键。 */
    private Long outboxId;
    /** 所属租户主键。 */
    private String tenantId;
    /** 订单号。 */
    private String orderNo;
    /** 预占单号。 */
    private String reservationNo;
    /** 操作类型。 */
    private String actionType;
    /** 操作人类型。 */
    private String operatorType;
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

    public InventoryAuditDeadLetter(Long id, Long outboxId, Long tenantId, String orderNo, String reservationNo,
                                    String actionType, String operatorType, Long operatorId, Instant occurredAt,
                                    Integer retryCount, String errorMessage, String deadReason, Instant deadAt) {
        this(id, outboxId, tenantId == null ? null : String.valueOf(tenantId), orderNo, reservationNo, actionType,
                operatorType, operatorId == null ? null : String.valueOf(operatorId), occurredAt, retryCount,
                errorMessage, deadReason, deadAt);
    }

    public InventoryAuditDeadLetter(Long id, Long outboxId, Long tenantId, String orderNo, String reservationNo,
                                    String actionType, String operatorType, Long operatorId, Instant occurredAt,
                                    Integer retryCount, String errorMessage, String deadReason, Instant deadAt,
                                    String replayStatus, Integer replayCount, Instant lastReplayAt,
                                    String lastReplayResult, String lastReplayError, String replayKey,
                                    String replayOperatorType, Long replayOperatorId) {
        this(id, outboxId, tenantId == null ? null : String.valueOf(tenantId), orderNo, reservationNo, actionType,
                operatorType, operatorId == null ? null : String.valueOf(operatorId), occurredAt, retryCount,
                errorMessage, deadReason, deadAt, replayStatus, replayCount, lastReplayAt, lastReplayResult,
                lastReplayError, replayKey, replayOperatorType,
                replayOperatorId == null ? null : String.valueOf(replayOperatorId));
    }

    public InventoryAuditDeadLetter(Long id, Long outboxId, String tenantId, String orderNo, String reservationNo,
                                    String actionType, String operatorType, String operatorId, Instant occurredAt,
                                    Integer retryCount, String errorMessage, String deadReason, Instant deadAt) {
        this(id, outboxId, tenantId, orderNo, reservationNo, actionType, operatorType, operatorId, occurredAt,
                retryCount, errorMessage, deadReason, deadAt, REPLAY_STATUS_PENDING, 0, null, null, null, null,
                null, null);
    }
}
