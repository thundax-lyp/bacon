package com.github.thundax.bacon.payment.application.assembler;

import com.github.thundax.bacon.payment.application.dto.PaymentAuditLogDTO;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;

public final class PaymentAuditLogAssembler {

    private PaymentAuditLogAssembler() {}

    public static PaymentAuditLogDTO toDto(PaymentAuditLog auditLog) {
        return new PaymentAuditLogDTO(
                auditLog.getPaymentNo().value(),
                auditLog.getActionType().value(),
                toStatusValue(auditLog.getBeforeStatus()),
                toStatusValue(auditLog.getAfterStatus()),
                auditLog.getOperatorType().value(),
                auditLog.getOperatorId(),
                auditLog.getOccurredAt());
    }

    private static String toStatusValue(PaymentStatus status) {
        return status == null ? null : status.value();
    }
}
