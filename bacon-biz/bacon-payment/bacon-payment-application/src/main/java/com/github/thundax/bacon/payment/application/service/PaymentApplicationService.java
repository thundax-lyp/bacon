package com.github.thundax.bacon.payment.application.service;

import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.domain.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class PaymentApplicationService {

    private final AtomicLong idGenerator = new AtomicLong(1L);
    private final PaymentRepository paymentRepository;

    public PaymentApplicationService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentCreateResultDTO createPayment(Long tenantId, String orderNo, Long userId, BigDecimal amount,
                                                String channelCode, String subject, Instant expiredAt) {
        String paymentNo = "PAY-" + UUID.randomUUID().toString().substring(0, 8);
        PaymentOrder paymentOrder = new PaymentOrder(idGenerator.getAndIncrement(), tenantId, paymentNo, orderNo, userId,
                channelCode, amount, subject, expiredAt, Instant.now());
        paymentRepository.save(paymentOrder);
        return new PaymentCreateResultDTO(tenantId, paymentNo, orderNo, channelCode, paymentOrder.getPaymentStatus(),
                "mock://pay/" + paymentNo, expiredAt, null);
    }
}
