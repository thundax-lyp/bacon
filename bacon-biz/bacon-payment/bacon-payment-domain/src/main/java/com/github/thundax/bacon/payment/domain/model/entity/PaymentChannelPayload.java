package com.github.thundax.bacon.payment.domain.model.entity;

import lombok.Getter;

@Getter
public class PaymentChannelPayload {

    private final String paymentNo;
    private final String channelCode;
    private final String payUrl;

    public PaymentChannelPayload(String paymentNo, String channelCode, String payUrl) {
        this.paymentNo = paymentNo;
        this.channelCode = channelCode;
        this.payUrl = payUrl;
    }
}
