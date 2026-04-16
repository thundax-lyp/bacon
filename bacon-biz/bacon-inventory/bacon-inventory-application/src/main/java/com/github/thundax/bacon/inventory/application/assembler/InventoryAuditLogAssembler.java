package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.codec.OperatorIdCodec;
import com.github.thundax.bacon.inventory.application.dto.InventoryAuditLogDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;

public final class InventoryAuditLogAssembler {

    private InventoryAuditLogAssembler() {}

    public static InventoryAuditLogDTO toDto(InventoryAuditLog auditLog) {
        return new InventoryAuditLogDTO(
                auditLog.getId(),
                auditLog.getOrderNo() == null ? null : auditLog.getOrderNo().value(),
                auditLog.getReservationNo() == null
                        ? null
                        : auditLog.getReservationNo().value(),
                auditLog.getActionType() == null
                        ? null
                        : auditLog.getActionType().value(),
                auditLog.getOperatorType() == null
                        ? null
                        : auditLog.getOperatorType().value(),
                OperatorIdCodec.toLongValue(auditLog.getOperatorId()),
                auditLog.getOccurredAt());
    }

    public static InventoryAuditLog toDomain(InventoryAuditLogDTO dto) {
        return InventoryAuditLog.reconstruct(
                dto.getId(),
                dto.getOrderNo() == null ? null : OrderNo.of(dto.getOrderNo()),
                dto.getReservationNo() == null ? null : ReservationNo.of(dto.getReservationNo()),
                dto.getActionType() == null ? null : InventoryAuditActionType.from(dto.getActionType()),
                dto.getOperatorType() == null ? null : InventoryAuditOperatorType.from(dto.getOperatorType()),
                OperatorIdCodec.toDomainFromLong(dto.getOperatorId()),
                dto.getOccurredAt());
    }
}
