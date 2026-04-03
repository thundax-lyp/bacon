package com.github.thundax.bacon.payment.domain.model.enums;

/**
 * 支付渠道状态。
 */
public enum PaymentChannelStatus {

    PAYING,
    SUCCESS,
    FAILED,
    CLOSED;

    public String value() {
        return name();
    }

    public static PaymentChannelStatus fromValue(String value) {
        return value == null ? null : PaymentChannelStatus.valueOf(value);
    }
}
