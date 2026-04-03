package com.github.thundax.bacon.order.domain.model.valueobject;

import com.github.thundax.bacon.common.id.domain.TenantId;

/**
 * 订单幂等记录业务键。
 */
public record OrderIdempotencyRecordKey(
        TenantId tenantId,
        OrderNo orderNo,
        String eventType
) {

    public OrderIdempotencyRecordKey {
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId must not be null");
        }
        if (orderNo == null) {
            throw new IllegalArgumentException("orderNo must not be null");
        }
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("eventType must not be blank");
        }
    }

    public static OrderIdempotencyRecordKey of(TenantId tenantId, OrderNo orderNo, String eventType) {
        return new OrderIdempotencyRecordKey(tenantId, orderNo, eventType);
    }
}
