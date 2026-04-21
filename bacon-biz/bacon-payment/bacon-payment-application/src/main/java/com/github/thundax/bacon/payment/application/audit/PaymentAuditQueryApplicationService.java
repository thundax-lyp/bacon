package com.github.thundax.bacon.payment.application.audit;

import com.github.thundax.bacon.payment.application.assembler.PaymentAuditLogAssembler;
import com.github.thundax.bacon.payment.application.dto.PaymentAuditLogDTO;
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
        return paymentAuditLogRepository.listLogsByPaymentNo(paymentNo).stream()
                .map(PaymentAuditLogAssembler::toDto)
                .toList();
    }
}
