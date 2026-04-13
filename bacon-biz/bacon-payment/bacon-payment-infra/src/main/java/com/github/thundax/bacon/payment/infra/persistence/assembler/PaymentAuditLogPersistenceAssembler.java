package com.github.thundax.bacon.payment.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditActionType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditOperatorType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.infra.persistence.dataobject.PaymentAuditLogDO;
import org.springframework.stereotype.Component;

@Component
public class PaymentAuditLogPersistenceAssembler {

    public PaymentAuditLogDO toDataObject(PaymentAuditLog auditLog) {
        return new PaymentAuditLogDO(
                auditLog.getId(),
                BaconContextHolder.requireTenantId(),
                auditLog.getPaymentNo() == null ? null : auditLog.getPaymentNo().value(),
                auditLog.getActionType() == null ? null : auditLog.getActionType().value(),
                auditLog.getBeforeStatus() == null ? null : auditLog.getBeforeStatus().value(),
                auditLog.getAfterStatus() == null ? null : auditLog.getAfterStatus().value(),
                auditLog.getOperatorType() == null ? null : auditLog.getOperatorType().value(),
                auditLog.getOperatorId(),
                auditLog.getOccurredAt());
    }

    public PaymentAuditLog toDomain(PaymentAuditLogDO dataObject) {
        return PaymentAuditLog.reconstruct(
                dataObject.getId(),
                dataObject.getPaymentNo() == null ? null : PaymentNo.of(dataObject.getPaymentNo()),
                dataObject.getActionType() == null ? null : PaymentAuditActionType.fromValue(dataObject.getActionType()),
                dataObject.getBeforeStatus() == null ? null : PaymentStatus.fromValue(dataObject.getBeforeStatus()),
                dataObject.getAfterStatus() == null ? null : PaymentStatus.fromValue(dataObject.getAfterStatus()),
                dataObject.getOperatorType() == null ? null : PaymentAuditOperatorType.fromValue(dataObject.getOperatorType()),
                dataObject.getOperatorId(),
                dataObject.getOccurredAt());
    }
}
