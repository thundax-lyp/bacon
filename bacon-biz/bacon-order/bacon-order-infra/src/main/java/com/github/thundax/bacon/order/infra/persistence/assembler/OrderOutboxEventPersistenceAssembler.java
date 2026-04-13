package com.github.thundax.bacon.order.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.order.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderOutboxEventDO;
import org.springframework.stereotype.Component;

@Component
public class OrderOutboxEventPersistenceAssembler {

    public OrderOutboxEventDO toDataObject(OrderOutboxEvent event) {
        return new OrderOutboxEventDO(
                event.getId() == null ? null : event.getId().value(),
                event.getEventCode() == null ? null : event.getEventCode().value(),
                BaconContextHolder.requireTenantId(),
                event.getOrderNo() == null ? null : event.getOrderNo().value(),
                event.getEventType() == null ? null : event.getEventType().value(),
                event.getBusinessKey(),
                event.getPayload(),
                event.getStatus() == null ? null : event.getStatus().value(),
                event.getRetryCount(),
                event.getNextRetryAt(),
                event.getProcessingOwner(),
                event.getLeaseUntil(),
                event.getClaimedAt(),
                event.getErrorMessage(),
                event.getDeadReason(),
                event.getCreatedAt(),
                event.getUpdatedAt());
    }

    public OrderOutboxEvent toDomain(OrderOutboxEventDO dataObject) {
        return OrderOutboxEvent.reconstruct(
                dataObject.getId() == null ? null : OutboxId.of(dataObject.getId()),
                dataObject.getEventCode() == null ? null : EventCode.of(dataObject.getEventCode()),
                dataObject.getOrderNo() == null ? null : OrderNo.of(dataObject.getOrderNo()),
                dataObject.getEventType() == null ? null : OrderOutboxEventType.from(dataObject.getEventType()),
                dataObject.getBusinessKey(),
                dataObject.getPayload(),
                dataObject.getStatus() == null ? null : OrderOutboxStatus.from(dataObject.getStatus()),
                dataObject.getRetryCount(),
                dataObject.getNextRetryAt(),
                dataObject.getProcessingOwner(),
                dataObject.getLeaseUntil(),
                dataObject.getClaimedAt(),
                dataObject.getErrorMessage(),
                dataObject.getDeadReason(),
                dataObject.getCreatedAt(),
                dataObject.getUpdatedAt());
    }
}
