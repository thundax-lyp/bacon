package com.github.thundax.bacon.payment.application.service;

import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.application.support.PaymentAuditLogSupport;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentChannelPayload;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import com.github.thundax.bacon.payment.domain.service.PaymentNoGenerator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class PaymentApplicationService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentAuditLogSupport paymentAuditLogSupport;
    private final PaymentNoGenerator paymentNoGenerator;

    public PaymentApplicationService(PaymentOrderRepository paymentOrderRepository,
                                     PaymentAuditLogSupport paymentAuditLogSupport,
                                     PaymentNoGenerator paymentNoGenerator) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentAuditLogSupport = paymentAuditLogSupport;
        this.paymentNoGenerator = paymentNoGenerator;
    }

    public PaymentCreateResultDTO createPayment(Long tenantId, String orderNo, Long userId, BigDecimal amount,
                                                String channelCode, String subject, Instant expiredAt) {
        validateCreateRequest(amount, channelCode, expiredAt);
        PaymentOrder existing = paymentOrderRepository.findOrderByOrderNo(tenantId, orderNo).orElse(null);
        if (existing != null) {
            return toCreateResult(existing, buildPayload(existing), null);
        }
        String paymentNo = paymentNoGenerator.nextPaymentNo();
        if (paymentNo == null || paymentNo.isBlank()) {
            throw new IllegalStateException("Payment no generator returned blank value");
        }
        PaymentOrder paymentOrder = new PaymentOrder(null, tenantId, paymentNo, orderNo, userId,
                channelCode, amount, subject, expiredAt, Instant.now());
        paymentOrder.markPaying();
        PaymentOrder persistedOrder = paymentOrderRepository.save(paymentOrder);
        paymentAuditLogSupport.saveSafely(new PaymentAuditLog(null, tenantId, paymentNo,
                PaymentAuditLog.ACTION_CREATE, null, paymentOrder.getPaymentStatus(),
                PaymentAuditLog.OPERATOR_SYSTEM, 0L, persistedOrder.getCreatedAt()));
        return toCreateResult(persistedOrder, buildPayload(persistedOrder), null);
    }

    private void validateCreateRequest(BigDecimal amount, String channelCode, Instant expiredAt) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (!PaymentOrder.CHANNEL_MOCK.equals(channelCode)) {
            throw new IllegalArgumentException("Unsupported channel code: " + channelCode);
        }
        if (expiredAt == null || !expiredAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("ExpiredAt must be in the future");
        }
    }

    private PaymentChannelPayload buildPayload(PaymentOrder paymentOrder) {
        return new PaymentChannelPayload(paymentOrder.getPaymentNo(), paymentOrder.getChannelCode(),
                "mock://pay/" + paymentOrder.getPaymentNo());
    }

    private PaymentCreateResultDTO toCreateResult(PaymentOrder paymentOrder,
                                                  PaymentChannelPayload channelPayload,
                                                  String failureReason) {
        String payPayload = PaymentOrder.STATUS_PAYING.equals(paymentOrder.getPaymentStatus()) ? channelPayload.getPayUrl() : null;
        Instant dtoExpiredAt = PaymentOrder.STATUS_PAYING.equals(paymentOrder.getPaymentStatus()) ? paymentOrder.getExpiredAt() : null;
        return new PaymentCreateResultDTO(paymentOrder.getTenantId(), paymentOrder.getPaymentNo(), paymentOrder.getOrderNo(),
                paymentOrder.getChannelCode(), paymentOrder.getPaymentStatus(), payPayload, dtoExpiredAt, failureReason);
    }
}
