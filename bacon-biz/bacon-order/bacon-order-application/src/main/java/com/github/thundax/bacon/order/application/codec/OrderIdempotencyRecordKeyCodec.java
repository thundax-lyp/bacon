package com.github.thundax.bacon.order.application.codec;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;

public final class OrderIdempotencyRecordKeyCodec {

    private OrderIdempotencyRecordKeyCodec() {}

    public static OrderIdempotencyRecordKey toDomain(String orderNo, String eventType) {
        return orderNo == null && eventType == null
                ? null
                : OrderIdempotencyRecordKey.of(orderNo == null ? null : OrderNo.of(orderNo), eventType);
    }

    public static String toOrderNoValue(OrderIdempotencyRecordKey value) {
        return value == null || value.orderNo() == null ? null : value.orderNo().value();
    }

    public static String toEventTypeValue(OrderIdempotencyRecordKey value) {
        return value == null ? null : value.eventType();
    }
}
