package com.github.thundax.bacon.payment.application.service;

import com.github.thundax.bacon.order.api.facade.OrderCommandFacade;
import com.github.thundax.bacon.payment.domain.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class PaymentCallbackApplicationService {

    private final PaymentRepository paymentRepository;
    private final OrderCommandFacade orderCommandFacade;

    public PaymentCallbackApplicationService(PaymentRepository paymentRepository, OrderCommandFacade orderCommandFacade) {
        this.paymentRepository = paymentRepository;
        this.orderCommandFacade = orderCommandFacade;
    }

    public void callbackPaid(String channelCode, Long tenantId, String paymentNo, String channelTransactionNo) {
        PaymentOrder paymentOrder = paymentRepository.findByPaymentNo(tenantId, paymentNo)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentNo));
        paymentOrder.markPaid(paymentOrder.getAmount(), Instant.now(), channelTransactionNo, "SUCCESS", "paid");
        orderCommandFacade.markPaid(tenantId, paymentOrder.getOrderNo(), paymentNo, channelCode,
                paymentOrder.getAmount(), Instant.now());
    }

    public void callbackFailed(String channelCode, Long tenantId, String paymentNo, String reason) {
        PaymentOrder paymentOrder = paymentRepository.findByPaymentNo(tenantId, paymentNo)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentNo));
        paymentOrder.markFailed("FAILED", reason);
        orderCommandFacade.markPaymentFailed(tenantId, paymentOrder.getOrderNo(), paymentNo, reason, "FAILED", Instant.now());
    }
}
