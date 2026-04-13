package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannel;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 订单支付快照。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderPaymentSnapshot {

    /** 快照主键。 */
    private Long id;
    /** 订单主键。 */
    private OrderId orderId;
    /** 支付单号。 */
    private PaymentNo paymentNo;
    /** 支付渠道编码。 */
    private PaymentChannel channelCode;
    /** 支付状态。 */
    private PayStatus payStatus;
    /** 已支付金额。 */
    private Money paidAmount;
    /** 支付完成时间。 */
    private Instant paidTime;
    /** 失败原因。 */
    private String failureReason;
    /** 支付渠道状态。 */
    private PaymentChannelStatus channelStatus;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public static OrderPaymentSnapshot create(
            OrderId orderId,
            PaymentNo paymentNo,
            PaymentChannel channelCode,
            PayStatus payStatus,
            Money paidAmount,
            Instant paidTime,
            String failureReason,
            PaymentChannelStatus channelStatus,
            Instant updatedAt) {
        return new OrderPaymentSnapshot(
                null,
                orderId,
                paymentNo,
                channelCode,
                payStatus,
                paidAmount,
                paidTime,
                failureReason,
                channelStatus,
                updatedAt);
    }

    public static OrderPaymentSnapshot reconstruct(
            Long id,
            OrderId orderId,
            PaymentNo paymentNo,
            PaymentChannel channelCode,
            PayStatus payStatus,
            Money paidAmount,
            Instant paidTime,
            String failureReason,
            PaymentChannelStatus channelStatus,
            Instant updatedAt) {
        return new OrderPaymentSnapshot(
                id,
                orderId,
                paymentNo,
                channelCode,
                payStatus,
                paidAmount,
                paidTime,
                failureReason,
                channelStatus,
                updatedAt);
    }
}
