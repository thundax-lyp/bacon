package com.github.thundax.bacon.order.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.enums.OperatorType;
import com.github.thundax.bacon.order.domain.model.enums.OrderAuditActionType;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderAuditLogDO;
import org.springframework.stereotype.Component;

@Component
public class OrderAuditLogPersistenceAssembler {

    public OrderAuditLogDO toDataObject(OrderAuditLog auditLog) {
        return new OrderAuditLogDO(
                auditLog.getId(),
                BaconContextHolder.requireTenantId(),
                auditLog.getOrderNo() == null ? null : auditLog.getOrderNo().value(),
                auditLog.getActionType() == null ? null : auditLog.getActionType().value(),
                auditLog.getBeforeStatus() == null ? null : auditLog.getBeforeStatus().value(),
                auditLog.getAfterStatus() == null ? null : auditLog.getAfterStatus().value(),
                auditLog.getOperatorType() == null ? null : auditLog.getOperatorType().value(),
                auditLog.getOperatorId(),
                auditLog.getOccurredAt());
    }

    public OrderAuditLog toDomain(OrderAuditLogDO dataObject) {
        return OrderAuditLog.reconstruct(
                dataObject.getId(),
                dataObject.getOrderNo() == null ? null : OrderNo.of(dataObject.getOrderNo()),
                dataObject.getActionType() == null ? null : OrderAuditActionType.from(dataObject.getActionType()),
                dataObject.getBeforeStatus() == null ? null : OrderStatus.from(dataObject.getBeforeStatus()),
                dataObject.getAfterStatus() == null ? null : OrderStatus.from(dataObject.getAfterStatus()),
                dataObject.getOperatorType() == null ? null : OperatorType.from(dataObject.getOperatorType()),
                dataObject.getOperatorId(),
                dataObject.getOccurredAt());
    }
}
