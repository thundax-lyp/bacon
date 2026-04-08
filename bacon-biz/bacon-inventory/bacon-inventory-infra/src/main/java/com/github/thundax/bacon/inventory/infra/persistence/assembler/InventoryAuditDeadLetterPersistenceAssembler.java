package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditDeadLetterDO;

public final class InventoryAuditDeadLetterPersistenceAssembler {

    private InventoryAuditDeadLetterPersistenceAssembler() {
    }

    public static InventoryAuditDeadLetterDO toDataObject(InventoryAuditDeadLetter deadLetter) {
        return new InventoryAuditDeadLetterDO(deadLetter.getIdValue(), deadLetter.getOutboxIdValue(),
                deadLetter.getEventCodeValue(), deadLetter.getTenantId() == null ? null : deadLetter.getTenantId().value(),
                deadLetter.getOrderNoValue(), deadLetter.getReservationNoValue(), deadLetter.getActionTypeValue(),
                deadLetter.getOperatorTypeValue(), deadLetter.getOperatorIdValue(), deadLetter.getOccurredAt(),
                deadLetter.getRetryCount(), deadLetter.getErrorMessage(), deadLetter.getDeadReason(),
                deadLetter.getDeadAt(), deadLetter.getReplayStatusValue(), deadLetter.getReplayCount(),
                deadLetter.getLastReplayAt(), deadLetter.getLastReplayResult(), deadLetter.getLastReplayError(),
                deadLetter.getReplayKey(), deadLetter.getReplayOperatorType(), deadLetter.getReplayOperatorIdValue());
    }

    public static InventoryAuditDeadLetter toDomain(InventoryAuditDeadLetterDO dataObject) {
        return new InventoryAuditDeadLetter(dataObject.getId(), dataObject.getOutboxId(), dataObject.getEventCode(),
                dataObject.getTenantId(), dataObject.getOrderNo(), dataObject.getReservationNo(),
                dataObject.getActionType(), dataObject.getOperatorType(), dataObject.getOperatorId(),
                dataObject.getOccurredAt(), dataObject.getRetryCount(), dataObject.getErrorMessage(), dataObject.getDeadReason(),
                dataObject.getDeadAt(), dataObject.getReplayStatus(), dataObject.getReplayCount(),
                dataObject.getLastReplayAt(), dataObject.getLastReplayResult(), dataObject.getLastReplayError(),
                dataObject.getReplayKey(), dataObject.getReplayOperatorType(),
                dataObject.getReplayOperatorId() == null ? null : String.valueOf(dataObject.getReplayOperatorId()));
    }
}
