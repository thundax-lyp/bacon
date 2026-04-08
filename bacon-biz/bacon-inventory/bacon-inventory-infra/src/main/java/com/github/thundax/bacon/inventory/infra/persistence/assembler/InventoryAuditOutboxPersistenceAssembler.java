package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOutboxStatus;
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
                dataObject.getId(),
                dataObject.getEventCode(),
                dataObject.getTenantId(),
                dataObject.getOrderNo(),
                dataObject.getReservationNo(),
                InventoryAuditActionType.from(dataObject.getActionType()),
                InventoryAuditOperatorType.from(dataObject.getOperatorType()),
                dataObject.getOperatorId(),
                dataObject.getOccurredAt(),
                dataObject.getErrorMessage(),
                InventoryAuditOutboxStatus.from(dataObject.getStatus()),
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
