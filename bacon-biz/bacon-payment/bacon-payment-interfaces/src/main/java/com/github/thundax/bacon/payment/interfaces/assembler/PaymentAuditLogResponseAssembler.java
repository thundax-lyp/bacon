package com.github.thundax.bacon.payment.interfaces.assembler;

import com.github.thundax.bacon.payment.api.dto.PaymentAuditLogDTO;
import com.github.thundax.bacon.payment.interfaces.response.PaymentAuditLogResponse;
import java.util.List;

public final class PaymentAuditLogResponseAssembler {

    private PaymentAuditLogResponseAssembler() {}

    public static List<PaymentAuditLogResponse> from(List<PaymentAuditLogDTO> auditLogs) {
        return auditLogs.stream()
                .map(dto -> new PaymentAuditLogResponse(
                        dto.tenantId(),
                        dto.paymentNo(),
                        dto.actionType(),
                        dto.beforeStatus(),
                        dto.afterStatus(),
                        dto.operatorType(),
                        dto.operatorId(),
                        dto.occurredAt()))
                .toList();
    }
}
