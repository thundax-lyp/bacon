package com.github.thundax.bacon.payment.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.PaymentOrderId;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 支付主单领域实体。
 */
@Getter
public class PaymentOrder {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_PAYING = "PAYING";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CLOSED = "CLOSED";
    public static final String CHANNEL_MOCK = "MOCK";

    /** 支付主单主键。 */
    private final PaymentOrderId id;
    /** 所属租户主键。 */
    private final Long tenantId;
    /** 支付单号。 */
    private final String paymentNo;
    /** 关联订单号。 */
    private final String orderNo;
    /** 支付用户主键。 */
    private final Long userId;
    /** 支付渠道编码。 */
    private final String channelCode;
    /** 支付金额。 */
    private final BigDecimal amount;
    /** 支付标题。 */
    private final String subject;
    /** 过期时间。 */
    private final Instant expiredAt;
    /** 创建时间。 */
    private final Instant createdAt;
    /** 支付状态。 */
    private String paymentStatus;
    /** 已支付金额。 */
    private BigDecimal paidAmount;
    /** 支付完成时间。 */
    private Instant paidAt;
    /** 关闭时间。 */
    private Instant closedAt;
    /** 支付渠道交易号。 */
    private String channelTransactionNo;
    /** 支付渠道状态。 */
    private String channelStatus;
    /** 最近一次回调摘要。 */
    private String callbackSummary;

    public PaymentOrder(PaymentOrderId id, Long tenantId, String paymentNo, String orderNo, Long userId, String channelCode,
                        BigDecimal amount, String subject, Instant expiredAt, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.paymentNo = paymentNo;
        this.orderNo = orderNo;
        this.userId = userId;
        this.channelCode = channelCode;
        this.amount = amount;
        this.subject = subject;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
        this.paymentStatus = STATUS_CREATED;
        this.paidAmount = BigDecimal.ZERO;
    }

    public static PaymentOrder rehydrate(PaymentOrderId id, Long tenantId, String paymentNo, String orderNo, Long userId,
                                         String channelCode, BigDecimal amount, BigDecimal paidAmount, String subject,
                                         Instant createdAt, Instant expiredAt, Instant paidAt, Instant closedAt,
                                         String paymentStatus, String channelTransactionNo, String channelStatus,
                                         String callbackSummary) {
        // 查询和回调处理依赖主单快照，因此重建时必须带回最新终态、渠道交易号和回调摘要。
        PaymentOrder paymentOrder = new PaymentOrder(id, tenantId, paymentNo, orderNo, userId, channelCode,
                amount, subject, expiredAt, createdAt);
        paymentOrder.paidAmount = paidAmount == null ? BigDecimal.ZERO : paidAmount;
        paymentOrder.paidAt = paidAt;
        paymentOrder.closedAt = closedAt;
        paymentOrder.paymentStatus = paymentStatus;
        paymentOrder.channelTransactionNo = channelTransactionNo;
        paymentOrder.channelStatus = channelStatus;
        paymentOrder.callbackSummary = callbackSummary;
        return paymentOrder;
    }

    public void markPaying() {
        // 创建支付单后允许把 CREATED 推进到 PAYING；终态收到重复指令时静默返回，幂等由主单承担。
        if (STATUS_PAID.equals(paymentStatus) || STATUS_FAILED.equals(paymentStatus) || STATUS_CLOSED.equals(paymentStatus)) {
            return;
        }
        this.paymentStatus = STATUS_PAYING;
    }

    public void markPaid(BigDecimal paidAmount, Instant paidTime, String channelTransactionNo, String channelStatus,
                         String callbackSummary) {
        // 支付成功一旦落主单就不可逆；重复成功回调只应被上层记审计，不应在实体内改写终态。
        if (STATUS_PAID.equals(paymentStatus) || STATUS_FAILED.equals(paymentStatus) || STATUS_CLOSED.equals(paymentStatus)) {
            return;
        }
        this.paymentStatus = STATUS_PAID;
        this.paidAmount = paidAmount;
        this.paidAt = paidTime;
        this.channelTransactionNo = channelTransactionNo;
        this.channelStatus = channelStatus;
        this.callbackSummary = callbackSummary;
    }

    public void markFailed(String channelStatus, String callbackSummary) {
        // 失败与成功、关闭互斥，进入 FAILED 后不再允许回到 PAYING，避免把终态再次暴露给渠道重试。
        if (STATUS_PAID.equals(paymentStatus) || STATUS_FAILED.equals(paymentStatus) || STATUS_CLOSED.equals(paymentStatus)) {
            return;
        }
        this.paymentStatus = STATUS_FAILED;
        this.channelStatus = channelStatus;
        this.callbackSummary = callbackSummary;
    }

    public void close(Instant closeTime) {
        // 关闭只针对未完成支付的主单；已支付/已失败的订单保持原终态，不被关单流程覆盖。
        if (STATUS_PAID.equals(paymentStatus) || STATUS_FAILED.equals(paymentStatus) || STATUS_CLOSED.equals(paymentStatus)) {
            return;
        }
        this.paymentStatus = STATUS_CLOSED;
        this.closedAt = closeTime;
    }
}
