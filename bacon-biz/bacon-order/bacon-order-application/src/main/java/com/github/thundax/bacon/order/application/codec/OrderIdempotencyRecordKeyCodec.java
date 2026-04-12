package com.github.thundax.bacon.order.application.codec;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;

public final class OrderIdempotencyRecordKeyCodec {

    private OrderIdempotencyRecordKeyCodec() {}

    public static OrderIdempotencyRecordKey toDomain(Long tenantId, String orderNo, String eventType) {
        return tenantId == null && orderNo == null && eventType == null
                ? null
                : OrderIdempotencyRecordKey.of(
                        tenantId == null ? null : TenantId.of(tenantId),
                        orderNo == null ? null : OrderNo.of(orderNo),
                        eventType);
    }

    public static Long toTenantIdValue(OrderIdempotencyRecordKey value) {
        return value == null || value.tenantId() == null ? null : value.tenantId().value();
    }

    public static String toOrderNoValue(OrderIdempotencyRecordKey value) {
        return value == null || value.orderNo() == null ? null : value.orderNo().value();
    }

    public static String toEventTypeValue(OrderIdempotencyRecordKey value) {
        return value == null ? null : value.eventType();
    }
}
