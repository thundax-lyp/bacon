package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannel;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import java.time.Instant;

/**
 * 订单支付快照。
 */
public record OrderPaymentSnapshot(
        /** 快照主键。 */
        Long id,
        /** 所属租户主键。 */
        TenantId tenantId,
        /** 订单主键。 */
        OrderId orderId,
        /** 支付单号。 */
        PaymentNo paymentNo,
        /** 支付渠道编码。 */
        PaymentChannel channelCode,
        /** 支付状态。 */
        PayStatus payStatus,
        /** 已支付金额。 */
        Money paidAmount,
        /** 支付完成时间。 */
        Instant paidTime,
        /** 失败原因。 */
        String failureReason,
        /** 支付渠道状态。 */
        PaymentChannelStatus channelStatus,
        /** 最后更新时间。 */
        Instant updatedAt) {

    public Long tenantIdValue() {
        return tenantId == null ? null : tenantId.value();
    }

    public Long orderIdValue() {
        return orderId == null ? null : orderId.value();
    }

    public String paymentNoValue() {
        return paymentNo == null ? null : paymentNo.value();
    }

    public String channelCodeValue() {
        return channelCode == null ? null : channelCode.value();
    }

    public String payStatusValue() {
        return payStatus == null ? null : payStatus.value();
    }

    public java.math.BigDecimal paidAmountValue() {
        return paidAmount == null ? null : paidAmount.value();
    }

    public String channelStatusValue() {
        return channelStatus == null ? null : channelStatus.value();
    }
}
