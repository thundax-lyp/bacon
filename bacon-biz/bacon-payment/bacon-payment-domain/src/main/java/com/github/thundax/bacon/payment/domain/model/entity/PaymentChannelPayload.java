package com.github.thundax.bacon.payment.domain.model.entity;

import lombok.Getter;

/**
 * 支付渠道拉起参数。
 */
@Getter
public class PaymentChannelPayload {

    /** 支付单号。 */
    private final String paymentNo;
    /** 支付渠道编码。 */
    private final String channelCode;
    /** 支付链接。 */
    private final String payUrl;

    public PaymentChannelPayload(String paymentNo, String channelCode, String payUrl) {
        this.paymentNo = paymentNo;
        this.channelCode = channelCode;
        this.payUrl = payUrl;
    }
}
