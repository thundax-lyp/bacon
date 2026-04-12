package com.github.thundax.bacon.common.commerce.codec;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;

public final class OrderNoCodec {

    private OrderNoCodec() {}

    public static OrderNo toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return OrderNo.of(value);
    }

    public static String toValue(OrderNo orderNo) {
        return orderNo == null ? null : orderNo.value();
    }
}
