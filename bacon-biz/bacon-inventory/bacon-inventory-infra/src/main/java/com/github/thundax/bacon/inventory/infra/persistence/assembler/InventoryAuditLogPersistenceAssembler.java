package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditLogDO;

public final class InventoryAuditLogPersistenceAssembler {

    private InventoryAuditLogPersistenceAssembler() {
    }

    public static InventoryAuditLog toDomain(InventoryAuditLogDO dataObject) {
        return new InventoryAuditLog(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderNo(),
                dataObject.getReservationNo(), dataObject.getActionType(), dataObject.getOperatorType(),
                dataObject.getOperatorId(), dataObject.getOccurredAt());
    }

    public static InventoryAuditLogDO toDataObject(InventoryAuditLog auditLog) {
        return new InventoryAuditLogDO(auditLog.getId(), auditLog.getTenantId() == null ? null : auditLog.getTenantId().value(), auditLog.getOrderNoValue(),
                auditLog.getReservationNoValue(), auditLog.getActionTypeValue(), auditLog.getOperatorTypeValue(),
                auditLog.getOperatorId(), auditLog.getOccurredAt());
    }
}
