package com.github.thundax.bacon.inventory.interfaces.response;

import java.time.Instant;
import java.util.List;

public record InventoryReservationResponse(Long tenantId, String orderNo, String reservationNo,
                                           String reservationStatus, Long warehouseId,
                                           List<InventoryReservationItemResponse> items, String failureReason,
                                           String releaseReason, Instant createdAt, Instant releasedAt,
                                           Instant deductedAt) {
}
