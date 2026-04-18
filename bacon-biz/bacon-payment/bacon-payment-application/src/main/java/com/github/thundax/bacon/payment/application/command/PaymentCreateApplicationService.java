package com.github.thundax.bacon.payment.application.command;

import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.payment.application.audit.PaymentOperationLogSupport;
import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentChannelPayload;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelCode;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.domain.model.valueobject.PaymentOrderId;
import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import com.github.thundax.bacon.payment.domain.service.PaymentNoGenerator;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentCreateApplicationService {

    private static final String PAYMENT_ORDER_ID_BIZ_TAG = "payment_order_id";

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentOperationLogSupport paymentOperationLogSupport;
    private final PaymentNoGenerator paymentNoGenerator;
    private final IdGenerator idGenerator;

    public PaymentCreateApplicationService(
            PaymentOrderRepository paymentOrderRepository,
            PaymentOperationLogSupport paymentOperationLogSupport,
            PaymentNoGenerator paymentNoGenerator,
            IdGenerator idGenerator) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentOperationLogSupport = paymentOperationLogSupport;
        this.paymentNoGenerator = paymentNoGenerator;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public PaymentCreateResult createPayment(
            String orderNo, Long userId, BigDecimal amount, String channelCode, String subject, Instant expiredAt) {
        BaconContextHolder.requireTenantId();
        validateCreateRequest(amount, channelCode, expiredAt);
        PaymentOrder existing = paymentOrderRepository.findByOrderNo(orderNo).orElse(null);
        // 按 orderNo 保证创建幂等；同一订单重复创建时直接返回已存在支付单，而不是重新生成 paymentNo。
        if (existing != null) {
            return toCreateResult(existing, buildPayload(existing), null);
        }
        String nextPaymentNo = paymentNoGenerator.nextPaymentNo();
        if (nextPaymentNo == null || nextPaymentNo.isBlank()) {
            throw new PaymentDomainException(PaymentErrorCode.PAYMENT_REMOTE_UNAVAILABLE, "payment-no-generator");
        }
        PaymentNo paymentNo = PaymentNo.of(nextPaymentNo);
        PaymentOrder paymentOrder = PaymentOrder.create(
                PaymentOrderId.of(idGenerator.nextId(PAYMENT_ORDER_ID_BIZ_TAG)),
                paymentNo,
                OrderNo.of(orderNo),
                UserIdCodec.toDomain(userId),
                PaymentChannelCode.fromValue(channelCode),
                Money.of(amount),
                subject,
                expiredAt,
                Instant.now());
        // 创建后立即进入 PAYING，表示渠道拉起参数已经准备好，后续只等待回调或显式关闭。
        paymentOrder.markPaying();
        PaymentOrder persistedOrder = paymentOrderRepository.insert(paymentOrder);
        paymentOperationLogSupport.recordCreate(
                paymentNo.value(), paymentOrder.getPaymentStatus().value(), persistedOrder.getCreatedAt());
        return toCreateResult(persistedOrder, buildPayload(persistedOrder), null);
    }

    private void validateCreateRequest(BigDecimal amount, String channelCode, Instant expiredAt) {
        if (amount == null || amount.signum() <= 0) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        PaymentChannelCode.fromValue(channelCode);
        if (expiredAt == null || !expiredAt.isAfter(Instant.now())) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_EXPIRED_AT);
        }
    }

    private PaymentChannelPayload buildPayload(PaymentOrder paymentOrder) {
        return new PaymentChannelPayload(
                paymentOrder.getPaymentNo(),
                paymentOrder.getChannelCode(),
                "mock://pay/" + paymentOrder.getPaymentNo().value());
    }

    private PaymentCreateResult toCreateResult(
            PaymentOrder paymentOrder, PaymentChannelPayload channelPayload, String failureReason) {
        // 只有处于 PAYING 的支付单才继续暴露 payPayload 和过期时间；终态单查询时不再返回重新拉起信息。
        String payPayload = PaymentStatus.PAYING == paymentOrder.getPaymentStatus() ? channelPayload.getPayUrl() : null;
        Instant dtoExpiredAt =
                PaymentStatus.PAYING == paymentOrder.getPaymentStatus() ? paymentOrder.getExpiredAt() : null;
        return new PaymentCreateResult(
                paymentOrder.getPaymentNo().value(),
                paymentOrder.getOrderNo().value(),
                paymentOrder.getChannelCode().value(),
                paymentOrder.getPaymentStatus().value(),
                payPayload,
                dtoExpiredAt,
                failureReason);
    }
}
