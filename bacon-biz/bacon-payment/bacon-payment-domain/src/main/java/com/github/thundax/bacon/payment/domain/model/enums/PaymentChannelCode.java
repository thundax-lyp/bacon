package com.github.thundax.bacon.payment.domain.model.enums;

import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import java.util.Arrays;

/**
 * 支付渠道编码。
 */
public enum PaymentChannelCode {
    MOCK;

    public String value() {
        return name();
    }

    public static PaymentChannelCode fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new PaymentDomainException(PaymentErrorCode.INVALID_CHANNEL_CODE, value));
    }
}
