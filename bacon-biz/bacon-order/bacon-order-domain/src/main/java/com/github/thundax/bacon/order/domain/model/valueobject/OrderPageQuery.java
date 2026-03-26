package com.github.thundax.bacon.order.domain.model.valueobject;

import java.time.Instant;

public record OrderPageQuery(
        Long tenantId,
        Long userId,
        String orderNo,
        String orderStatus,
        String payStatus,
        String inventoryStatus,
        Instant createdAtFrom,
        Instant createdAtTo,
        int offset,
        int limit
) {
}
