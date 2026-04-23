package com.github.thundax.bacon.inventory.api.response;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 库存预占门面响应。
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationFacadeResponse {

    private String orderNo;

    private String reservationNo;

    private String reservationStatus;

    private String inventoryStatus;

    private String warehouseCode;

    private List<InventoryReservationItemFacadeResponse> items;

    private String failureReason;

    private String releaseReason;

    private Instant createdAt;

    private Instant releasedAt;

    private Instant deductedAt;
}
