package com.github.thundax.bacon.payment.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelCode;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.domain.model.valueobject.PaymentOrderId;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 支付主单领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentOrder {

    /** 支付主单主键。 */
    private PaymentOrderId id;
    /** 支付单号。 */
    private PaymentNo paymentNo;
    /** 关联订单号。 */
    private OrderNo orderNo;
    /** 支付用户主键。 */
    private UserId userId;
    /** 支付渠道编码。 */
    private PaymentChannelCode channelCode;
    /** 支付金额。 */
    private Money amount;
    /** 支付标题。 */
    private String subject;
    /** 过期时间。 */
    private Instant expiredAt;
    /** 创建时间。 */
    private Instant createdAt;
    /** 支付状态。 */
    private PaymentStatus paymentStatus;
    /** 已支付金额。 */
    private Money paidAmount;
    /** 支付完成时间。 */
    private Instant paidAt;
    /** 关闭时间。 */
    private Instant closedAt;
    /** 支付渠道交易号。 */
    private String channelTransactionNo;
    /** 支付渠道状态。 */
    private PaymentChannelStatus channelStatus;
    /** 最近一次回调摘要。 */
    private String callbackSummary;

    public static PaymentOrder create(
            PaymentOrderId id,
            PaymentNo paymentNo,
            OrderNo orderNo,
            UserId userId,
            PaymentChannelCode channelCode,
            Money amount,
            String subject,
            Instant expiredAt,
            Instant createdAt) {
        return new PaymentOrder(
                id,
                paymentNo,
                orderNo,
                userId,
                channelCode,
                amount,
                subject,
                expiredAt,
                createdAt,
                PaymentStatus.CREATED,
                Money.zero(),
                null,
                null,
                null,
                null,
                null);
    }

    public static PaymentOrder reconstruct(
            PaymentOrderId id,
            PaymentNo paymentNo,
            OrderNo orderNo,
            UserId userId,
            PaymentChannelCode channelCode,
            Money amount,
            Money paidAmount,
            String subject,
            Instant createdAt,
            Instant expiredAt,
            Instant paidAt,
            Instant closedAt,
            PaymentStatus paymentStatus,
            String channelTransactionNo,
            PaymentChannelStatus channelStatus,
            String callbackSummary) {
        // 查询和回调处理依赖主单快照，因此重建时必须带回最新终态、渠道交易号和回调摘要。
        return new PaymentOrder(
                id,
                paymentNo,
                orderNo,
                userId,
                channelCode,
                amount,
                subject,
                expiredAt,
                createdAt,
                paymentStatus,
                paidAmount == null ? Money.zero() : paidAmount,
                paidAt,
                closedAt,
                channelTransactionNo,
                channelStatus,
                callbackSummary);
    }

    public void markPaying() {
        // 创建支付单后允许把 CREATED 推进到 PAYING；终态收到重复指令时静默返回，幂等由主单承担。
        if (PaymentStatus.PAID == paymentStatus
                || PaymentStatus.FAILED == paymentStatus
                || PaymentStatus.CLOSED == paymentStatus) {
            return;
        }
        this.paymentStatus = PaymentStatus.PAYING;
    }

    public void markPaid(
            Money paidAmount,
            Instant paidTime,
            String channelTransactionNo,
            PaymentChannelStatus channelStatus,
            String callbackSummary) {
        // 支付成功一旦落主单就不可逆；重复成功回调只应被上层记审计，不应在实体内改写终态。
        if (PaymentStatus.PAID == paymentStatus
                || PaymentStatus.FAILED == paymentStatus
                || PaymentStatus.CLOSED == paymentStatus) {
            return;
        }
        this.paymentStatus = PaymentStatus.PAID;
        this.paidAmount = paidAmount;
        this.paidAt = paidTime;
        this.channelTransactionNo = channelTransactionNo;
        this.channelStatus = channelStatus;
        this.callbackSummary = callbackSummary;
    }

    public void markFailed(PaymentChannelStatus channelStatus, String callbackSummary) {
        // 失败与成功、关闭互斥，进入 FAILED 后不再允许回到 PAYING，避免把终态再次暴露给渠道重试。
        if (PaymentStatus.PAID == paymentStatus
                || PaymentStatus.FAILED == paymentStatus
                || PaymentStatus.CLOSED == paymentStatus) {
            return;
        }
        this.paymentStatus = PaymentStatus.FAILED;
        this.channelStatus = channelStatus;
        this.callbackSummary = callbackSummary;
    }

    public void close(Instant closeTime) {
        // 关闭只针对未完成支付的主单；已支付/已失败的订单保持原终态，不被关单流程覆盖。
        if (PaymentStatus.PAID == paymentStatus
                || PaymentStatus.FAILED == paymentStatus
                || PaymentStatus.CLOSED == paymentStatus) {
            return;
        }
        this.paymentStatus = PaymentStatus.CLOSED;
        this.closedAt = closeTime;
    }
}
