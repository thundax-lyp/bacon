package com.github.thundax.bacon.order.domain.model.enums;

/**
 * 订单出站事件类型。
 */
public enum OrderOutboxEventType {

    RESERVE_STOCK("RESERVE_STOCK"),
    CREATE_PAYMENT("CREATE_PAYMENT"),
    RELEASE_STOCK("RELEASE_STOCK");

    private final String value;

    OrderOutboxEventType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static OrderOutboxEventType fromValue(String value) {
        for (OrderOutboxEventType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported order outbox event type: " + value);
    }
}
