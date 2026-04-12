package com.github.thundax.bacon.order.application.codec;

import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;

public final class OrderIdCodec {

    private OrderIdCodec() {}

    public static OrderId toDomain(Long value) {
        return value == null ? null : OrderId.of(value);
    }

    public static Long toValue(OrderId value) {
        return value == null ? null : value.value();
    }
}
