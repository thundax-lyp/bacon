package com.github.thundax.bacon.payment.domain.model.enums;

import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;

/**
 * 支付渠道编码。
 */
public enum PaymentChannelCode {

    MOCK;

    public String value() {
        return name();
    }

    public static PaymentChannelCode fromValue(String value) {
        try {
            return value == null ? null : PaymentChannelCode.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new PaymentDomainException(PaymentErrorCode.INVALID_CHANNEL_CODE, value);
        }
    }
}
