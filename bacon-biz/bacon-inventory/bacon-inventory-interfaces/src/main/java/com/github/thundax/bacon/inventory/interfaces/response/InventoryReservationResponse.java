package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import java.time.Instant;
import java.util.List;

public record InventoryReservationResponse(Long tenantId, String orderNo, String reservationNo,
                                           String reservationStatus, Long warehouseId,
                                           List<InventoryReservationItemResponse> items, String failureReason,
                                           String releaseReason, Instant createdAt, Instant releasedAt,
                                           Instant deductedAt) {

    public static InventoryReservationResponse from(InventoryReservationDTO dto) {
        return new InventoryReservationResponse(dto.getTenantId(), dto.getOrderNo(), dto.getReservationNo(),
                dto.getReservationStatus(), dto.getWarehouseId(), dto.getItems().stream()
                .map(InventoryReservationItemResponse::from)
                .toList(), dto.getFailureReason(), dto.getReleaseReason(), dto.getCreatedAt(), dto.getReleasedAt(),
                dto.getDeductedAt());
    }
}
