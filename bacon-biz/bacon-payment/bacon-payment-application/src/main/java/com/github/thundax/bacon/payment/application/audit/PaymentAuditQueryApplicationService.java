package com.github.thundax.bacon.payment.application.audit;

import com.github.thundax.bacon.payment.application.dto.PaymentAuditLogDTO;
import com.github.thundax.bacon.payment.application.audit.PaymentAuditLogQuery;
import com.github.thundax.bacon.payment.domain.repository.PaymentAuditLogRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PaymentAuditQueryApplicationService {

    private final PaymentAuditLogRepository paymentAuditLogRepository;

    public PaymentAuditQueryApplicationService(PaymentAuditLogRepository paymentAuditLogRepository) {
        this.paymentAuditLogRepository = paymentAuditLogRepository;
    }

    public List<PaymentAuditLogDTO> getByPaymentNo(PaymentAuditLogQuery query) {
        return paymentAuditLogRepository.listLogsByPaymentNo(query.paymentNo()).stream()
                .map(paymentAuditLog -> new PaymentAuditLogDTO(
                        paymentAuditLog.getPaymentNo().value(),
                        paymentAuditLog.getActionType().value(),
                        paymentAuditLog.getBeforeStatus() == null ? null : paymentAuditLog.getBeforeStatus().value(),
                        paymentAuditLog.getAfterStatus() == null ? null : paymentAuditLog.getAfterStatus().value(),
                        paymentAuditLog.getOperatorType().value(),
                        paymentAuditLog.getOperatorId(),
                        paymentAuditLog.getOccurredAt()))
                .toList();
    }
}
