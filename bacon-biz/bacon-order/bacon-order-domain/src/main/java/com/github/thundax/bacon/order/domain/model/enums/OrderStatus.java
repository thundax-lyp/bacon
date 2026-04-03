package com.github.thundax.bacon.order.domain.model.enums;

/**
 * 订单主状态。
 */
public enum OrderStatus {

    CREATED("CREATED"),
    RESERVING_STOCK("RESERVING_STOCK"),
    PENDING_PAYMENT("PENDING_PAYMENT"),
    PAID("PAID"),
    CANCELLED("CANCELLED"),
    CLOSED("CLOSED");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static OrderStatus fromValue(String value) {
        for (OrderStatus orderStatus : values()) {
            if (orderStatus.value.equals(value)) {
                return orderStatus;
            }
        }
        throw new IllegalArgumentException("Unsupported order status: " + value);
    }
}
