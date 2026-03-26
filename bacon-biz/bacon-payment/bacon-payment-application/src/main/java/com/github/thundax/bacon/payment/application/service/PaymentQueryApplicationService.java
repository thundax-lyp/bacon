package com.github.thundax.bacon.payment.application.service;

import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentQueryApplicationService {

    private final PaymentRepository paymentRepository;

    public PaymentQueryApplicationService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentDetailDTO getByPaymentNo(Long tenantId, String paymentNo) {
        return toDetail(paymentRepository.findByPaymentNo(tenantId, paymentNo)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentNo)));
    }

    public PaymentDetailDTO getByOrderNo(Long tenantId, String orderNo) {
        return toDetail(paymentRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + orderNo)));
    }

    private PaymentDetailDTO toDetail(PaymentOrder paymentOrder) {
        return new PaymentDetailDTO(paymentOrder.getTenantId(), paymentOrder.getPaymentNo(), paymentOrder.getOrderNo(),
                paymentOrder.getUserId(), paymentOrder.getChannelCode(), paymentOrder.getPaymentStatus(),
                paymentOrder.getAmount(), paymentOrder.getPaidAmount(), paymentOrder.getCreatedAt(),
                paymentOrder.getExpiredAt(), paymentOrder.getPaidAt(), paymentOrder.getSubject(),
                paymentOrder.getClosedAt(), paymentOrder.getChannelTransactionNo(), paymentOrder.getChannelStatus(),
                paymentOrder.getCallbackSummary());
    }
}
