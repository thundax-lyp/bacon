package com.github.thundax.bacon.payment.application.audit;

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
                .map(auditLog -> new PaymentAuditLogDTO(auditLog.getTenantId(), auditLog.getPaymentNo(),
                        auditLog.getActionType(), auditLog.getBeforeStatus(), auditLog.getAfterStatus(),
                        auditLog.getOperatorType(), auditLog.getOperatorId(), auditLog.getOccurredAt()))
                .toList();
    }
}
