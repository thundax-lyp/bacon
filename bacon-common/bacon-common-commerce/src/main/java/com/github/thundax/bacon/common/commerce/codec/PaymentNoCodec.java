package com.github.thundax.bacon.common.commerce.codec;

import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;

public final class PaymentNoCodec {

    private PaymentNoCodec() {}

    public static PaymentNo toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return PaymentNo.of(value);
    }

    public static String toValue(PaymentNo paymentNo) {
        return paymentNo == null ? null : paymentNo.value();
    }
}
