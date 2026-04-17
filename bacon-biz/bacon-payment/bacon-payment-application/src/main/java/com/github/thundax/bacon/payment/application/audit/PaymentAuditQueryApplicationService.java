package com.github.thundax.bacon.payment.application.audit;

import com.github.thundax.bacon.payment.application.dto.PaymentAuditLogDTO;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.domain.repository.PaymentAuditLogRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PaymentAuditQueryApplicationService {

    private final PaymentAuditLogRepository paymentAuditLogRepository;

    public PaymentAuditQueryApplicationService(PaymentAuditLogRepository paymentAuditLogRepository) {
        this.paymentAuditLogRepository = paymentAuditLogRepository;
    }

    public List<PaymentAuditLogDTO> getByPaymentNo(String paymentNo) {
        return paymentAuditLogRepository.findAuditLogsByPaymentNo(paymentNo).stream()
                .map(auditLog -> new PaymentAuditLogDTO(
                        auditLog.getPaymentNo().value(),
                        auditLog.getActionType().value(),
                        toStatusValue(auditLog.getBeforeStatus()),
                        toStatusValue(auditLog.getAfterStatus()),
                        auditLog.getOperatorType().value(),
                        auditLog.getOperatorId(),
                        auditLog.getOccurredAt()))
                .toList();
    }

    private String toStatusValue(PaymentStatus status) {
        return status == null ? null : status.value();
    }
}
