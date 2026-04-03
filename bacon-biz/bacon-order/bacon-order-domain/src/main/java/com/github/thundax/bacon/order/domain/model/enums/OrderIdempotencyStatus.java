package com.github.thundax.bacon.order.domain.model.enums;

/**
 * 订单幂等处理状态。
 */
public enum OrderIdempotencyStatus {

    PROCESSING("PROCESSING"),
    SUCCESS("SUCCESS"),
    FAILED("FAILED");

    private final String value;

    OrderIdempotencyStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static OrderIdempotencyStatus fromValue(String value) {
        for (OrderIdempotencyStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown order idempotency status: " + value);
    }
}
