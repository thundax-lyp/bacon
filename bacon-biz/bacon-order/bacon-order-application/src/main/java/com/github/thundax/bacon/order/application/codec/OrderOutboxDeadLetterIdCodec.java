package com.github.thundax.bacon.order.application.codec;

import com.github.thundax.bacon.order.domain.model.valueobject.OrderOutboxDeadLetterId;

public final class OrderOutboxDeadLetterIdCodec {

    private OrderOutboxDeadLetterIdCodec() {}

    public static OrderOutboxDeadLetterId toDomain(Long value) {
        return value == null ? null : OrderOutboxDeadLetterId.of(value);
    }

    public static Long toValue(OrderOutboxDeadLetterId value) {
        return value == null ? null : value.value();
    }
}
