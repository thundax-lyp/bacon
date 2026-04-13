package com.github.thundax.bacon.order.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxDeadLetter;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxReplayStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.order.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderOutboxDeadLetterDO;
import org.springframework.stereotype.Component;

@Component
public class OrderOutboxDeadLetterPersistenceAssembler {

    public OrderOutboxDeadLetterDO toDataObject(OrderOutboxDeadLetter deadLetter) {
        return new OrderOutboxDeadLetterDO(
                deadLetter.getId(),
                deadLetter.getOutboxId() == null
                        ? null
                        : deadLetter.getOutboxId().value(),
                deadLetter.getEventCode() == null
                        ? null
                        : deadLetter.getEventCode().value(),
                BaconContextHolder.requireTenantId(),
                deadLetter.getOrderNo() == null ? null : deadLetter.getOrderNo().value(),
                deadLetter.getEventType() == null
                        ? null
                        : deadLetter.getEventType().value(),
                deadLetter.getBusinessKey(),
                deadLetter.getPayload(),
                deadLetter.getRetryCount(),
                deadLetter.getErrorMessage(),
                deadLetter.getDeadReason(),
                deadLetter.getDeadAt(),
                deadLetter.getReplayStatus() == null
                        ? null
                        : deadLetter.getReplayStatus().value(),
                deadLetter.getReplayCount(),
                deadLetter.getLastReplayAt(),
                deadLetter.getLastReplayMessage(),
                deadLetter.getCreatedAt(),
                deadLetter.getUpdatedAt());
    }

    public OrderOutboxDeadLetter toDomain(OrderOutboxDeadLetterDO dataObject) {
        return OrderOutboxDeadLetter.reconstruct(
                dataObject.getId(),
                dataObject.getOutboxId() == null ? null : OutboxId.of(dataObject.getOutboxId()),
                dataObject.getEventCode() == null ? null : EventCode.of(dataObject.getEventCode()),
                dataObject.getOrderNo() == null ? null : OrderNo.of(dataObject.getOrderNo()),
                dataObject.getEventType() == null ? null : OrderOutboxEventType.from(dataObject.getEventType()),
                dataObject.getBusinessKey(),
                dataObject.getPayload(),
                dataObject.getRetryCount(),
                dataObject.getErrorMessage(),
                dataObject.getDeadReason(),
                dataObject.getDeadAt(),
                dataObject.getReplayStatus() == null
                        ? null
                        : OrderOutboxReplayStatus.from(dataObject.getReplayStatus()),
                dataObject.getReplayCount(),
                dataObject.getLastReplayAt(),
                dataObject.getLastReplayMessage(),
                dataObject.getCreatedAt(),
                dataObject.getUpdatedAt());
    }
}
