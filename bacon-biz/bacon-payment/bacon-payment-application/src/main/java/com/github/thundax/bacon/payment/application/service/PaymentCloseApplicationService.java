package com.github.thundax.bacon.payment.application.service;

import com.github.thundax.bacon.payment.api.dto.PaymentCloseResultDTO;
import com.github.thundax.bacon.payment.application.support.PaymentAuditLogSupport;
import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
public class PaymentCloseApplicationService {

    private static final Set<String> VALID_REASONS = Set.of("USER_CANCELLED", "SYSTEM_CANCELLED", "TIMEOUT_CLOSED");
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentAuditLogSupport paymentAuditLogSupport;

    public PaymentCloseApplicationService(PaymentOrderRepository paymentOrderRepository,
                                          PaymentAuditLogSupport paymentAuditLogSupport) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentAuditLogSupport = paymentAuditLogSupport;
    }

    public PaymentCloseResultDTO closePayment(Long tenantId, String paymentNo, String reason) {
        if (!VALID_REASONS.contains(reason)) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CLOSE_REASON, reason);
        }
        PaymentOrder paymentOrder = paymentOrderRepository.findOrderByPaymentNo(tenantId, paymentNo)
                .orElseThrow(() -> new PaymentDomainException(PaymentErrorCode.PAYMENT_NOT_FOUND, paymentNo));
        if (PaymentOrder.STATUS_CLOSED.equals(paymentOrder.getPaymentStatus())) {
            return new PaymentCloseResultDTO(tenantId, paymentNo, paymentOrder.getOrderNo(),
                    paymentOrder.getPaymentStatus(), "SUCCESS", reason, null);
        }
        if (PaymentOrder.STATUS_PAID.equals(paymentOrder.getPaymentStatus())) {
            return new PaymentCloseResultDTO(tenantId, paymentNo, paymentOrder.getOrderNo(),
                    paymentOrder.getPaymentStatus(), "FAILED", reason, "Paid payment cannot be closed");
        }
        if (PaymentOrder.STATUS_FAILED.equals(paymentOrder.getPaymentStatus())) {
            return new PaymentCloseResultDTO(tenantId, paymentNo, paymentOrder.getOrderNo(),
                    paymentOrder.getPaymentStatus(), "FAILED", reason, "Failed payment cannot be closed");
        }
        String beforeStatus = paymentOrder.getPaymentStatus();
        Instant closedAt = Instant.now();
        paymentOrder.close(closedAt);
        paymentOrderRepository.save(paymentOrder);
        paymentAuditLogSupport.saveSafely(new PaymentAuditLog(null, tenantId, paymentNo,
                PaymentAuditLog.ACTION_CLOSE, beforeStatus, paymentOrder.getPaymentStatus(),
                PaymentAuditLog.OPERATOR_SYSTEM, 0L, closedAt));
        return new PaymentCloseResultDTO(tenantId, paymentNo, paymentOrder.getOrderNo(),
                paymentOrder.getPaymentStatus(), "SUCCESS", reason, null);
    }
}
