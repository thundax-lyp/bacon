package com.github.thundax.bacon.payment.application.audit;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.payment.api.dto.PaymentAuditLogDTO;
import com.github.thundax.bacon.payment.domain.repository.PaymentAuditLogRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PaymentAuditQueryApplicationService {

    private final PaymentAuditLogRepository paymentAuditLogRepository;

    public PaymentAuditQueryApplicationService(PaymentAuditLogRepository paymentAuditLogRepository) {
        this.paymentAuditLogRepository = paymentAuditLogRepository;
    }

    public List<PaymentAuditLogDTO> getByPaymentNo(Long tenantId, String paymentNo) {
        return paymentAuditLogRepository.findAuditLogsByPaymentNo(tenantId, paymentNo)
                .stream()
                .map(auditLog -> new PaymentAuditLogDTO(toLongTenantValue(auditLog.getTenantId()), auditLog.getPaymentNo().value(),
                        auditLog.getActionType().value(), toStatusValue(auditLog.getBeforeStatus()),
                        toStatusValue(auditLog.getAfterStatus()), auditLog.getOperatorType().value(), auditLog.getOperatorId(),
                        auditLog.getOccurredAt()))
                .toList();
    }

    private Long toLongTenantValue(TenantId tenantId) {
        return tenantId == null ? null : tenantId.value();
    }

    private String toStatusValue(com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus status) {
        return status == null ? null : status.value();
    }
}
