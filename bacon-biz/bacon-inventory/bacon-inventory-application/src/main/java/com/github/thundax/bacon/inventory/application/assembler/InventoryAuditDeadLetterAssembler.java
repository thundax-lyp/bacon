package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditDeadLetterDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;

public final class InventoryAuditDeadLetterAssembler {

    private InventoryAuditDeadLetterAssembler() {
    }

    public static InventoryAuditDeadLetterDTO toDto(InventoryAuditDeadLetter deadLetter) {
        return new InventoryAuditDeadLetterDTO(
                deadLetter.getIdValue(),
                deadLetter.getOutboxIdValue(),
                deadLetter.getEventCodeValue(),
                deadLetter.getTenantId() == null ? null : deadLetter.getTenantId().value(),
                deadLetter.getOrderNoValue(),
                deadLetter.getReservationNoValue(),
                deadLetter.getActionTypeValue(),
                deadLetter.getOperatorTypeValue(),
                deadLetter.getOperatorId(),
                deadLetter.getOccurredAt(),
                deadLetter.getRetryCount(),
                deadLetter.getErrorMessage(),
                deadLetter.getDeadReason(),
                deadLetter.getDeadAt(),
                deadLetter.getReplayStatusValue(),
                deadLetter.getReplayCount(),
                deadLetter.getLastReplayAt(),
                deadLetter.getLastReplayResult(),
                deadLetter.getLastReplayError(),
                deadLetter.getReplayKey(),
                deadLetter.getReplayOperatorType(),
                deadLetter.getReplayOperatorId());
    }

    public static InventoryAuditDeadLetter toDomain(InventoryAuditDeadLetterDTO dto) {
        return new InventoryAuditDeadLetter(
                dto.getId() == null ? null : DeadLetterId.of(dto.getId()),
                dto.getOutboxId() == null ? null : OutboxId.of(dto.getOutboxId()),
                dto.getEventCode() == null ? null : EventCode.of(dto.getEventCode()),
                dto.getTenantId() == null ? null : TenantId.of(dto.getTenantId()),
                dto.getOrderNo() == null ? null : OrderNo.of(dto.getOrderNo()),
                dto.getReservationNo() == null ? null : ReservationNo.of(dto.getReservationNo()),
                dto.getActionType() == null ? null : InventoryAuditActionType.from(dto.getActionType()),
                dto.getOperatorType() == null ? null : InventoryAuditOperatorType.from(dto.getOperatorType()),
                dto.getOperatorId(),
                dto.getOccurredAt(),
                dto.getRetryCount(),
                dto.getErrorMessage(),
                dto.getDeadReason(),
                dto.getDeadAt(),
                dto.getReplayStatus() == null ? null : InventoryAuditReplayStatus.from(dto.getReplayStatus()),
                dto.getReplayCount(),
                dto.getLastReplayAt(),
                dto.getLastReplayResult(),
                dto.getLastReplayError(),
                dto.getReplayKey(),
                dto.getReplayOperatorType(),
                dto.getReplayOperatorId());
    }
}
