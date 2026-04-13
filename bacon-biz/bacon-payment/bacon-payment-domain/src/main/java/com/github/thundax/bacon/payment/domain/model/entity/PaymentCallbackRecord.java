package com.github.thundax.bacon.payment.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelCode;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelStatus;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 支付渠道回调记录。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentCallbackRecord {

    /** 回调记录主键。 */
    private Long id;
    /** 支付单号。 */
    private PaymentNo paymentNo;
    /** 关联订单号。 */
    private OrderNo orderNo;
    /** 支付渠道编码。 */
    private PaymentChannelCode channelCode;
    /** 支付渠道交易号。 */
    private String channelTransactionNo;
    /** 支付渠道状态。 */
    private PaymentChannelStatus channelStatus;
    /** 原始回调载荷。 */
    private String rawPayload;
    /** 接收时间。 */
    private Instant receivedAt;

    public static PaymentCallbackRecord create(
            Long id,
            PaymentNo paymentNo,
            OrderNo orderNo,
            PaymentChannelCode channelCode,
            String channelTransactionNo,
            PaymentChannelStatus channelStatus,
            String rawPayload,
            Instant receivedAt) {
        return new PaymentCallbackRecord(
                id, paymentNo, orderNo, channelCode, channelTransactionNo, channelStatus, rawPayload, receivedAt);
    }

    public static PaymentCallbackRecord reconstruct(
            Long id,
            PaymentNo paymentNo,
            OrderNo orderNo,
            PaymentChannelCode channelCode,
            String channelTransactionNo,
            PaymentChannelStatus channelStatus,
            String rawPayload,
            Instant receivedAt) {
        return new PaymentCallbackRecord(
                id, paymentNo, orderNo, channelCode, channelTransactionNo, channelStatus, rawPayload, receivedAt);
    }

    public String summarize() {
        if (rawPayload == null || rawPayload.isBlank()) {
            return channelStatus == null ? null : channelStatus.value();
        }
        return rawPayload.length() <= 255 ? rawPayload : rawPayload.substring(0, 255);
    }
}
