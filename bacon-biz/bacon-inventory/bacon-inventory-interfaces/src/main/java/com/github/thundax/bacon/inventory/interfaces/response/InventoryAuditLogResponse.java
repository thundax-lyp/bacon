package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryAuditLogDTO;
import java.time.Instant;

public record InventoryAuditLogResponse(Long id, Long tenantId, String orderNo, String reservationNo,
                                        String actionType, String operatorType, Long operatorId,
                                        Instant occurredAt) {

    public static InventoryAuditLogResponse from(InventoryAuditLogDTO dto) {
        return new InventoryAuditLogResponse(dto.getId(), dto.getTenantId(), dto.getOrderNo(), dto.getReservationNo(),
                dto.getActionType(), dto.getOperatorType(), dto.getOperatorId(), dto.getOccurredAt());
    }
}
