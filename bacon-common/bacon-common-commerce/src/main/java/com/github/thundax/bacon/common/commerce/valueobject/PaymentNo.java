package com.github.thundax.bacon.common.commerce.valueobject;

/**
 * 支付业务单号。
 */
public record PaymentNo(String value) {

    public PaymentNo {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("paymentNo must not be blank");
        }
    }

    public static PaymentNo of(String value) {
        return new PaymentNo(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
