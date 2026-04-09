package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOutboxStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditOutboxDO;

public final class InventoryAuditOutboxPersistenceAssembler {

    private InventoryAuditOutboxPersistenceAssembler() {
    }

    public static InventoryAuditOutboxDO toDataObject(InventoryAuditOutbox outbox) {
        return new InventoryAuditOutboxDO(outbox.getIdValue(), outbox.getEventCodeValue(), outbox.getTenantId() == null ? null : outbox.getTenantId().value(),
                outbox.getOrderNoValue(), outbox.getReservationNoValue(), outbox.getActionTypeValue(),
                outbox.getOperatorTypeValue(),
                outbox.getOperatorIdValue(), outbox.getOccurredAt(), outbox.getErrorMessage(),
                outbox.getStatusValue(), outbox.getRetryCount(), outbox.getNextRetryAt(), outbox.getProcessingOwner(),
                outbox.getLeaseUntil(), outbox.getClaimedAt(), outbox.getDeadReason(), outbox.getFailedAt(),
                outbox.getUpdatedAt());
    }

    public static InventoryAuditOutbox toDomain(InventoryAuditOutboxDO dataObject) {
        return new InventoryAuditOutbox(
                dataObject.getId() == null ? null : OutboxId.of(dataObject.getId()),
                dataObject.getEventCode() == null ? null : EventCode.of(dataObject.getEventCode()),
                dataObject.getTenantId() == null ? null : TenantId.of(dataObject.getTenantId()),
                dataObject.getOrderNo() == null ? null : OrderNo.of(dataObject.getOrderNo()),
                dataObject.getReservationNo() == null ? null : ReservationNo.of(dataObject.getReservationNo()),
                dataObject.getActionType() == null ? null : InventoryAuditActionType.from(dataObject.getActionType()),
                dataObject.getOperatorType() == null ? null : InventoryAuditOperatorType.from(dataObject.getOperatorType()),
                dataObject.getOperatorId() == null ? null : String.valueOf(dataObject.getOperatorId()),
                dataObject.getOccurredAt(),
                dataObject.getErrorMessage(),
                dataObject.getStatus() == null ? null : InventoryAuditOutboxStatus.from(dataObject.getStatus()),
                dataObject.getRetryCount(),
                dataObject.getNextRetryAt(),
                dataObject.getProcessingOwner(),
                dataObject.getLeaseUntil(),
                dataObject.getClaimedAt(),
                dataObject.getDeadReason(),
                dataObject.getFailedAt(),
                dataObject.getUpdatedAt());
    }
}
