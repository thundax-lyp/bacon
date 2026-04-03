package com.github.thundax.bacon.order.domain.model.valueobject;

/**
 * 订单业务单号。
 */
public record OrderNo(String value) {

    public OrderNo {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("orderNo must not be blank");
        }
    }

    public static OrderNo of(String value) {
        return new OrderNo(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
