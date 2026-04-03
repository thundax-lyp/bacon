package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryAuditDeadLetterDTO;
import java.time.Instant;

/**
 * 库存审计死信响应对象。
 */
public record InventoryAuditDeadLetterResponse(
        /** 死信记录主键。 */
        String id,
        /** 审计出站主键。 */
        String outboxId,
        /** 所属租户主键。 */
        String tenantId,
        /** 订单号。 */
        String orderNo,
        /** 预占单号。 */
        String reservationNo,
        /** 操作类型。 */
        String actionType,
        /** 操作人类型。 */
        String operatorType,
        /** 操作人主键。 */
        Long operatorId,
        /** 原始发生时间。 */
        Instant occurredAt,
        /** 重试次数。 */
        Integer retryCount,
        /** 错误信息。 */
        String errorMessage,
        /** 死信原因。 */
        String deadReason,
        /** 死信时间。 */
        Instant deadAt,
        /** 回放状态。 */
        String replayStatus,
        /** 回放次数。 */
        Integer replayCount,
        /** 最近一次回放时间。 */
        Instant lastReplayAt,
        /** 最近一次回放结果。 */
        String lastReplayResult,
        /** 最近一次回放错误。 */
        String lastReplayError,
        /** 回放幂等键。 */
        String replayKey,
        /** 回放操作人类型。 */
        String replayOperatorType,
        /** 回放操作人主键。 */
        String replayOperatorId) {

    public static InventoryAuditDeadLetterResponse from(InventoryAuditDeadLetterDTO dto) {
        return new InventoryAuditDeadLetterResponse(dto.getId(), dto.getOutboxId(), dto.getTenantId(),
                dto.getOrderNo(), dto.getReservationNo(), dto.getActionType(), dto.getOperatorType(),
                dto.getOperatorId(), dto.getOccurredAt(), dto.getRetryCount(), dto.getErrorMessage(),
                dto.getDeadReason(), dto.getDeadAt(), dto.getReplayStatus(), dto.getReplayCount(),
                dto.getLastReplayAt(), dto.getLastReplayResult(), dto.getLastReplayError(), dto.getReplayKey(),
                dto.getReplayOperatorType(), dto.getReplayOperatorId());
    }
}
