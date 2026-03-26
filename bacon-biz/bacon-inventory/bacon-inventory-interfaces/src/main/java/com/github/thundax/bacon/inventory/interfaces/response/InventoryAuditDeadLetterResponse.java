package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryAuditDeadLetterDTO;
import java.time.Instant;

public record InventoryAuditDeadLetterResponse(
        Long id,
        Long outboxId,
        Long tenantId,
        String orderNo,
        String reservationNo,
        String actionType,
        String operatorType,
        Long operatorId,
        Instant occurredAt,
        Integer retryCount,
        String errorMessage,
        String deadReason,
        Instant deadAt,
        String replayStatus,
        Integer replayCount,
        Instant lastReplayAt,
        String lastReplayResult,
        String lastReplayError,
        String replayKey,
        String replayOperatorType,
        Long replayOperatorId) {

    public static InventoryAuditDeadLetterResponse from(InventoryAuditDeadLetterDTO dto) {
        return new InventoryAuditDeadLetterResponse(dto.getId(), dto.getOutboxId(), dto.getTenantId(),
                dto.getOrderNo(), dto.getReservationNo(), dto.getActionType(), dto.getOperatorType(),
                dto.getOperatorId(), dto.getOccurredAt(), dto.getRetryCount(), dto.getErrorMessage(),
                dto.getDeadReason(), dto.getDeadAt(), dto.getReplayStatus(), dto.getReplayCount(),
                dto.getLastReplayAt(), dto.getLastReplayResult(), dto.getLastReplayError(), dto.getReplayKey(),
                dto.getReplayOperatorType(), dto.getReplayOperatorId());
    }
}
