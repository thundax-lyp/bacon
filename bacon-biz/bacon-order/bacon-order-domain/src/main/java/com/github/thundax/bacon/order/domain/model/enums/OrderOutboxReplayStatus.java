package com.github.thundax.bacon.order.domain.model.enums;

/**
 * 订单出站死信回放状态。
 */
public enum OrderOutboxReplayStatus {

    PENDING("PENDING"),
    SUCCESS("SUCCESS"),
    FAILED("FAILED");

    private final String value;

    OrderOutboxReplayStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static OrderOutboxReplayStatus fromValue(String value) {
        for (OrderOutboxReplayStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported order outbox replay status: " + value);
    }
}
