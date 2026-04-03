package com.github.thundax.bacon.order.domain.model.enums;

/**
 * 支付状态。
 */
public enum PayStatus {

    UNPAID("UNPAID"),
    PAYING("PAYING"),
    PAID("PAID"),
    FAILED("FAILED"),
    CLOSED("CLOSED");

    private final String value;

    PayStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static PayStatus fromValue(String value) {
        for (PayStatus payStatus : values()) {
            if (payStatus.value.equals(value)) {
                return payStatus;
            }
        }
        throw new IllegalArgumentException("Unsupported pay status: " + value);
    }
}
