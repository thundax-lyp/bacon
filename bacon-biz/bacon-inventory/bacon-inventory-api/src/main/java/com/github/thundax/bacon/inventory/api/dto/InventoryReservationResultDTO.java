package com.github.thundax.bacon.inventory.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationResultDTO {

    private Long tenantId;
    private String orderNo;
    private String reservationNo;
    private String reservationStatus;
    private String inventoryStatus;
    private Long warehouseId;
    private String failureReason;
    private String releaseReason;
    private Instant releasedAt;
    private Instant deductedAt;
}
