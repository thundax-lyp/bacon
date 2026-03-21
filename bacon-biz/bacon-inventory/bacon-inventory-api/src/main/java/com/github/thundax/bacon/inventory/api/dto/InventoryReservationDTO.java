package com.github.thundax.bacon.inventory.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationDTO {

    private Long tenantId;
    private String orderNo;
    private String reservationNo;
    private String reservationStatus;
    private Long warehouseId;
    private List<InventoryReservationItemDTO> items;
    private String failureReason;
    private String releaseReason;
    private Instant createdAt;
    private Instant releasedAt;
    private Instant deductedAt;
}
