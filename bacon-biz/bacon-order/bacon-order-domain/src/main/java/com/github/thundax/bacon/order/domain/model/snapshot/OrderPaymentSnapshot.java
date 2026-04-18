package com.github.thundax.bacon.order.domain.model.snapshot;

import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannel;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import java.time.Instant;

/**
 * 订单支付快照。
 */
public record OrderPaymentSnapshot(
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
