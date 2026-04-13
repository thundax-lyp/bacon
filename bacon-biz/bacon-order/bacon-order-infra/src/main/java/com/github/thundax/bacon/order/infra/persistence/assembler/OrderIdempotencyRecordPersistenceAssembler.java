package com.github.thundax.bacon.order.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderIdempotencyRecordDO;
import org.springframework.stereotype.Component;

@Component
public class OrderIdempotencyRecordPersistenceAssembler {

    public OrderIdempotencyRecordDO toDataObject(OrderIdempotencyRecord record, Long tenantId) {
        return new OrderIdempotencyRecordDO(
                tenantId,
                record.getOrderNo() == null ? null : record.getOrderNo().value(),
                record.getEventType(),
                record.getStatus() == null ? null : record.getStatus().value(),
                record.getAttemptCount(),
                record.getLastError(),
                record.getProcessingOwner(),
                record.getLeaseUntil(),
                record.getClaimedAt(),
                record.getCreatedAt(),
                record.getUpdatedAt());
    }

    public OrderIdempotencyRecord toDomain(OrderIdempotencyRecordDO dataObject) {
        return OrderIdempotencyRecord.reconstruct(
                OrderIdempotencyRecordKey.of(
                        dataObject.getOrderNo() == null ? null : OrderNo.of(dataObject.getOrderNo()),
                        dataObject.getEventType()),
                dataObject.getStatus() == null ? null : OrderIdempotencyStatus.from(dataObject.getStatus()),
                dataObject.getAttemptCount(),
                dataObject.getLastError(),
                dataObject.getProcessingOwner(),
                dataObject.getLeaseUntil(),
                dataObject.getClaimedAt(),
                dataObject.getCreatedAt(),
                dataObject.getUpdatedAt());
    }
}
