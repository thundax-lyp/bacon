package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditLogDO;

public final class InventoryAuditLogPersistenceAssembler {

    private InventoryAuditLogPersistenceAssembler() {}

    public static InventoryAuditLog toDomain(InventoryAuditLogDO dataObject) {
        return new InventoryAuditLog(
                dataObject.getId(),
                dataObject.getTenantId() == null ? null : TenantId.of(dataObject.getTenantId()),
                dataObject.getOrderNo() == null ? null : OrderNo.of(dataObject.getOrderNo()),
                dataObject.getReservationNo() == null ? null : ReservationNo.of(dataObject.getReservationNo()),
                dataObject.getActionType() == null ? null : InventoryAuditActionType.from(dataObject.getActionType()),
                dataObject.getOperatorType() == null
                        ? null
                        : InventoryAuditOperatorType.from(dataObject.getOperatorType()),
                dataObject.getOperatorId(),
                dataObject.getOccurredAt());
    }

    public static InventoryAuditLogDO toDataObject(InventoryAuditLog auditLog) {
        return new InventoryAuditLogDO(
                auditLog.getId(),
                auditLog.getTenantId() == null ? null : auditLog.getTenantId().value(),
                auditLog.getOrderNoValue(),
                auditLog.getReservationNoValue(),
                auditLog.getActionTypeValue(),
                auditLog.getOperatorTypeValue(),
                auditLog.getOperatorId(),
                auditLog.getOccurredAt());
    }
}
