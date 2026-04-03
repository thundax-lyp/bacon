package com.github.thundax.bacon.payment.domain.model.enums;

import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;

/**
 * 支付渠道编码。
 */
public enum PaymentChannelCode {

    MOCK("MOCK");

    private final String value;

    PaymentChannelCode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static PaymentChannelCode fromValue(String value) {
        for (PaymentChannelCode channelCode : values()) {
            if (channelCode.value.equals(value)) {
                return channelCode;
            }
        }
        throw new PaymentDomainException(PaymentErrorCode.INVALID_CHANNEL_CODE, value);
    }
}
