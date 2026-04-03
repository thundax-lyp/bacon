package com.github.thundax.bacon.payment.domain.model.enums;

/**
 * 支付状态。
 */
public enum PaymentStatus {

    CREATED,
    PAYING,
    PAID,
    FAILED,
    CLOSED;

    public String value() {
        return name();
    }

    public static PaymentStatus fromValue(String value) {
        return value == null ? null : PaymentStatus.valueOf(value);
    }
}
