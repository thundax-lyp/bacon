package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.mapper.OperatorIdMapper;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditLogDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;

public final class InventoryAuditLogAssembler {

    private InventoryAuditLogAssembler() {}

    public static InventoryAuditLogDTO toDto(InventoryAuditLog auditLog) {
        return new InventoryAuditLogDTO(
                auditLog.getId(),
                auditLog.getTenantId() == null ? null : auditLog.getTenantId().value(),
                auditLog.getOrderNoValue(),
                auditLog.getReservationNoValue(),
                auditLog.getActionTypeValue(),
                auditLog.getOperatorTypeValue(),
                OperatorIdMapper.toLongValue(auditLog.getOperatorId()),
                auditLog.getOccurredAt());
    }

    public static InventoryAuditLog toDomain(InventoryAuditLogDTO dto) {
        return InventoryAuditLog.reconstruct(
                dto.getId(),
                dto.getTenantId() == null ? null : TenantId.of(dto.getTenantId()),
                dto.getOrderNo() == null ? null : OrderNo.of(dto.getOrderNo()),
                dto.getReservationNo() == null ? null : ReservationNo.of(dto.getReservationNo()),
                dto.getActionType() == null ? null : InventoryAuditActionType.from(dto.getActionType()),
                dto.getOperatorType() == null ? null : InventoryAuditOperatorType.from(dto.getOperatorType()),
                OperatorIdMapper.toDomainFromLong(dto.getOperatorId()),
                dto.getOccurredAt());
    }
}
