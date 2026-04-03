package com.github.thundax.bacon.order.domain.model.enums;

/**
 * 支付渠道状态。
 */
public enum PaymentChannelStatus {

    PAYING("PAYING"),
    SUCCESS("SUCCESS"),
    FAILED("FAILED"),
    CLOSED("CLOSED");

    private final String value;

    PaymentChannelStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static PaymentChannelStatus fromValue(String value) {
        for (PaymentChannelStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported payment channel status: " + value);
    }
}
