package com.github.thundax.bacon.payment.domain.model.entity;

import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelCode;
import lombok.Getter;

/**
 * 支付渠道拉起参数。
 */
@Getter
public class PaymentChannelPayload {

    /** 支付单号。 */
    private final PaymentNo paymentNo;
    /** 支付渠道编码。 */
    private final PaymentChannelCode channelCode;
    /** 支付链接。 */
    private final String payUrl;

    public PaymentChannelPayload(PaymentNo paymentNo, PaymentChannelCode channelCode, String payUrl) {
        this.paymentNo = paymentNo;
        this.channelCode = channelCode;
        this.payUrl = payUrl;
    }
}
