package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
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
        return new InventoryAuditDeadLetter(dataObject.getId() == null ? null : DeadLetterId.of(dataObject.getId()),
                dataObject.getOutboxId() == null ? null : OutboxId.of(dataObject.getOutboxId()),
                dataObject.getEventCode() == null ? null : EventCode.of(dataObject.getEventCode()),
                dataObject.getTenantId() == null ? null : TenantId.of(dataObject.getTenantId()),
                dataObject.getOrderNo() == null ? null : OrderNo.of(dataObject.getOrderNo()),
                dataObject.getReservationNo() == null ? null : ReservationNo.of(dataObject.getReservationNo()),
                dataObject.getActionType() == null ? null : InventoryAuditActionType.from(dataObject.getActionType()),
                dataObject.getOperatorType() == null ? null : InventoryAuditOperatorType.from(dataObject.getOperatorType()),
                dataObject.getOperatorId() == null ? null : String.valueOf(dataObject.getOperatorId()),
                dataObject.getOccurredAt(), dataObject.getRetryCount(), dataObject.getErrorMessage(), dataObject.getDeadReason(),
                dataObject.getDeadAt(),
                dataObject.getReplayStatus() == null ? null : InventoryAuditReplayStatus.from(dataObject.getReplayStatus()),
                dataObject.getReplayCount(),
                dataObject.getLastReplayAt(), dataObject.getLastReplayResult(), dataObject.getLastReplayError(),
                dataObject.getReplayKey(), dataObject.getReplayOperatorType(),
                dataObject.getReplayOperatorId() == null ? null : String.valueOf(dataObject.getReplayOperatorId()));
    }
}
