package com.github.thundax.bacon.payment.domain.model.entity;

import lombok.Getter;

import java.time.Instant;

/**
 * 支付渠道回调记录。
 */
@Getter
public class PaymentCallbackRecord {

    /** 回调记录主键。 */
    private final Long id;
    /** 所属租户主键。 */
    private final Long tenantId;
    /** 支付单号。 */
    private final String paymentNo;
    /** 关联订单号。 */
    private final String orderNo;
    /** 支付渠道编码。 */
    private final String channelCode;
    /** 支付渠道交易号。 */
    private final String channelTransactionNo;
    /** 支付渠道状态。 */
    private final String channelStatus;
    /** 原始回调载荷。 */
    private final String rawPayload;
    /** 接收时间。 */
    private final Instant receivedAt;

    public PaymentCallbackRecord(Long id, Long tenantId, String paymentNo, String orderNo, String channelCode,
                                 String channelTransactionNo, String channelStatus, String rawPayload, Instant receivedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.paymentNo = paymentNo;
        this.orderNo = orderNo;
        this.channelCode = channelCode;
        this.channelTransactionNo = channelTransactionNo;
        this.channelStatus = channelStatus;
        this.rawPayload = rawPayload;
        this.receivedAt = receivedAt;
    }

    public String summarize() {
        if (rawPayload == null || rawPayload.isBlank()) {
            return channelStatus;
        }
        return rawPayload.length() <= 255 ? rawPayload : rawPayload.substring(0, 255);
    }
}
