package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.codec.OperatorIdCodec;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditLogDO;

public final class InventoryAuditLogPersistenceAssembler {

    private InventoryAuditLogPersistenceAssembler() {}

    public static InventoryAuditLog toDomain(InventoryAuditLogDO dataObject) {
        return InventoryAuditLog.reconstruct(
                dataObject.getId(),
                dataObject.getOrderNo() == null ? null : OrderNo.of(dataObject.getOrderNo()),
                dataObject.getReservationNo() == null ? null : ReservationNo.of(dataObject.getReservationNo()),
                dataObject.getActionType() == null ? null : InventoryAuditActionType.from(dataObject.getActionType()),
                dataObject.getOperatorType() == null
                        ? null
                        : InventoryAuditOperatorType.from(dataObject.getOperatorType()),
                OperatorIdCodec.toDomainFromLong(dataObject.getOperatorId()),
                dataObject.getOccurredAt());
    }

    public static InventoryAuditLogDO toDataObject(InventoryAuditLog auditLog) {
        return new InventoryAuditLogDO(
                auditLog.getId(),
                BaconContextHolder.requireTenantId(),
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
}
