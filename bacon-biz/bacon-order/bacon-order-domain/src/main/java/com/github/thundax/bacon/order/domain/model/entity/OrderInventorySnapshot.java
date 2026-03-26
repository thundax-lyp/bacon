package com.github.thundax.bacon.order.domain.model.entity;

import java.time.Instant;

public record OrderInventorySnapshot(
        Long id,
        Long tenantId,
        Long orderId,
        String reservationNo,
        String inventoryStatus,
        Long warehouseId,
        String failureReason,
        Instant updatedAt
) {
}
