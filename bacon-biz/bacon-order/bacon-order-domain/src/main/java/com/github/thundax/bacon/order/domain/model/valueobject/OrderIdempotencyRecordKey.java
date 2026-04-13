package com.github.thundax.bacon.order.domain.model.valueobject;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;

/**
 * 订单幂等记录业务键。
 */
public record OrderIdempotencyRecordKey(OrderNo orderNo, String eventType) {

    public OrderIdempotencyRecordKey {
        if (orderNo == null) {
            throw new IllegalArgumentException("orderNo must not be null");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType must not be blank");
        }
    }

    public static OrderIdempotencyRecordKey of(OrderNo orderNo, String eventType) {
        return new OrderIdempotencyRecordKey(orderNo, eventType);
    }
}
