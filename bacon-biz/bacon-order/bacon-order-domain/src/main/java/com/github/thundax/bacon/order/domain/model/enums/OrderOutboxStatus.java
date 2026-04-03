package com.github.thundax.bacon.order.domain.model.enums;

/**
 * 订单出站事件状态。
 */
public enum OrderOutboxStatus {

    NEW("NEW"),
    RETRYING("RETRYING"),
    PROCESSING("PROCESSING"),
    DEAD("DEAD");

    private final String value;

    OrderOutboxStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static OrderOutboxStatus fromValue(String value) {
        for (OrderOutboxStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported order outbox status: " + value);
    }
}
