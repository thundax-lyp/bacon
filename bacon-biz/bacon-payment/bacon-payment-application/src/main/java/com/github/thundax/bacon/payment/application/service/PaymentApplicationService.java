package com.github.thundax.bacon.payment.application.service;

import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.domain.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentRepository;
import com.github.thundax.bacon.payment.domain.service.PaymentNoGenerator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PaymentApplicationService {

    private final AtomicLong idGenerator = new AtomicLong(1L);
    private final PaymentRepository paymentRepository;
    private final PaymentNoGenerator paymentNoGenerator;

    public PaymentApplicationService(PaymentRepository paymentRepository, PaymentNoGenerator paymentNoGenerator) {
        this.paymentRepository = paymentRepository;
        this.paymentNoGenerator = paymentNoGenerator;
    }

    public PaymentCreateResultDTO createPayment(Long tenantId, String orderNo, Long userId, BigDecimal amount,
                                                String channelCode, String subject, Instant expiredAt) {
        String paymentNo = paymentNoGenerator.nextPaymentNo();
        PaymentOrder paymentOrder = new PaymentOrder(idGenerator.getAndIncrement(), tenantId, paymentNo, orderNo, userId,
                channelCode, amount, subject, expiredAt, Instant.now());
        paymentRepository.save(paymentOrder);
        return new PaymentCreateResultDTO(tenantId, paymentNo, orderNo, channelCode, paymentOrder.getPaymentStatus(),
                "mock://pay/" + paymentNo, expiredAt, null);
    }
}
