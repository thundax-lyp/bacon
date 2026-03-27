package com.github.thundax.bacon.payment.application.command;

import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.application.audit.PaymentOperationLogSupport;
import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentChannelPayload;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import com.github.thundax.bacon.payment.domain.service.PaymentNoGenerator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class PaymentCreateApplicationService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentOperationLogSupport paymentOperationLogSupport;
    private final PaymentNoGenerator paymentNoGenerator;

    public PaymentCreateApplicationService(PaymentOrderRepository paymentOrderRepository,
                                           PaymentOperationLogSupport paymentOperationLogSupport,
                                           PaymentNoGenerator paymentNoGenerator) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentOperationLogSupport = paymentOperationLogSupport;
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
            throw new PaymentDomainException(PaymentErrorCode.PAYMENT_REMOTE_UNAVAILABLE, "payment-no-generator");
        }
        PaymentOrder paymentOrder = new PaymentOrder(null, tenantId, paymentNo, orderNo, userId,
                channelCode, amount, subject, expiredAt, Instant.now());
        paymentOrder.markPaying();
        PaymentOrder persistedOrder = paymentOrderRepository.save(paymentOrder);
        paymentOperationLogSupport.recordCreate(tenantId, paymentNo, paymentOrder.getPaymentStatus(),
                persistedOrder.getCreatedAt());
        return toCreateResult(persistedOrder, buildPayload(persistedOrder), null);
    }

    private void validateCreateRequest(BigDecimal amount, String channelCode, Instant expiredAt) {
        if (amount == null || amount.signum() <= 0) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        if (!PaymentOrder.CHANNEL_MOCK.equals(channelCode)) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CHANNEL_CODE, channelCode);
        }
        if (expiredAt == null || !expiredAt.isAfter(Instant.now())) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_EXPIRED_AT);
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
