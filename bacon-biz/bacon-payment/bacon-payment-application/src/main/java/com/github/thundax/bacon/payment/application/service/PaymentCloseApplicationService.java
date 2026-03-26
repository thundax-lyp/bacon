package com.github.thundax.bacon.payment.application.service;

import com.github.thundax.bacon.payment.api.dto.PaymentCloseResultDTO;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PaymentCloseApplicationService {

    private final PaymentRepository paymentRepository;

    public PaymentCloseApplicationService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentCloseResultDTO closePayment(Long tenantId, String paymentNo, String reason) {
        PaymentOrder paymentOrder = paymentRepository.findByPaymentNo(tenantId, paymentNo)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentNo));
        paymentOrder.close(Instant.now());
        return new PaymentCloseResultDTO(tenantId, paymentNo, paymentOrder.getOrderNo(),
                paymentOrder.getPaymentStatus(), "SUCCESS", reason, null);
    }
}
