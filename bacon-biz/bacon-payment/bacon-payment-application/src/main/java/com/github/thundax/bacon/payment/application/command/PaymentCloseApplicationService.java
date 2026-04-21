package com.github.thundax.bacon.payment.application.command;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.payment.application.audit.PaymentOperationLogSupport;
import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentCloseApplicationService {

    private static final Set<String> VALID_REASONS = Set.of("USER_CANCELLED", "SYSTEM_CANCELLED", "TIMEOUT_CLOSED");
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentOperationLogSupport paymentOperationLogSupport;

    public PaymentCloseApplicationService(
            PaymentOrderRepository paymentOrderRepository, PaymentOperationLogSupport paymentOperationLogSupport) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentOperationLogSupport = paymentOperationLogSupport;
    }

    @Transactional
    public PaymentCloseResult closePayment(PaymentCloseCommand command) {
        BaconContextHolder.requireTenantId();
        if (!VALID_REASONS.contains(command.reason())) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CLOSE_REASON, command.reason());
        }
        PaymentOrder paymentOrder = paymentOrderRepository
                .findByPaymentNo(command.paymentNo())
                .orElseThrow(() -> new PaymentDomainException(PaymentErrorCode.PAYMENT_NOT_FOUND, command.paymentNo()));
        // 已关闭视为幂等成功；已支付和已失败则显式拒绝关闭，避免把终态单误判成可关闭状态。
        if (PaymentStatus.CLOSED == paymentOrder.getPaymentStatus()) {
            return new PaymentCloseResult(
                    command.paymentNo(),
                    paymentOrder.getOrderNo().value(),
                    paymentOrder.getPaymentStatus().value(),
                    "SUCCESS",
                    command.reason(),
                    null);
        }
        if (PaymentStatus.PAID == paymentOrder.getPaymentStatus()) {
            return new PaymentCloseResult(
                    command.paymentNo(),
                    paymentOrder.getOrderNo().value(),
                    paymentOrder.getPaymentStatus().value(),
                    "FAILED",
                    command.reason(),
                    "Paid payment cannot be closed");
        }
        if (PaymentStatus.FAILED == paymentOrder.getPaymentStatus()) {
            return new PaymentCloseResult(
                    command.paymentNo(),
                    paymentOrder.getOrderNo().value(),
                    paymentOrder.getPaymentStatus().value(),
                    "FAILED",
                    command.reason(),
                    "Failed payment cannot be closed");
        }
        String beforeStatus = paymentOrder.getPaymentStatus().value();
        Instant closedAt = Instant.now();
        paymentOrder.close(closedAt);
        paymentOrderRepository.update(paymentOrder);
        paymentOperationLogSupport.recordClose(
                command.paymentNo(), beforeStatus, paymentOrder.getPaymentStatus().value(), closedAt);
        return new PaymentCloseResult(
                command.paymentNo(),
                paymentOrder.getOrderNo().value(),
                paymentOrder.getPaymentStatus().value(),
                "SUCCESS",
                command.reason(),
                null);
    }
}
