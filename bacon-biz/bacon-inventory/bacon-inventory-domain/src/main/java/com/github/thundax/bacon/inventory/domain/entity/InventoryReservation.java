package com.github.thundax.bacon.inventory.domain.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class InventoryReservation {

    private final Long id;
    private final Long tenantId;
    private final String reservationNo;
    private final String orderNo;
    private final Long warehouseId;
    private final Instant createdAt;
    private final List<InventoryReservationItem> items;
    private String reservationStatus;
    private String failureReason;
    private String releaseReason;
    private Instant releasedAt;
    private Instant deductedAt;

    public InventoryReservation(Long id, Long tenantId, String reservationNo, String orderNo, Long warehouseId,
                                Instant createdAt, List<InventoryReservationItem> items) {
        this.id = id;
        this.tenantId = tenantId;
        this.reservationNo = reservationNo;
        this.orderNo = orderNo;
        this.warehouseId = warehouseId;
        this.createdAt = createdAt;
        this.items = items;
        this.reservationStatus = "CREATED";
    }

    public void reserve() {
        this.reservationStatus = "RESERVED";
    }

    public void fail(String reason) {
        this.reservationStatus = "FAILED";
        this.failureReason = reason;
    }

    public void release(String reason, Instant releasedTime) {
        this.reservationStatus = "RELEASED";
        this.releaseReason = reason;
        this.releasedAt = releasedTime;
    }

    public void deduct(Instant deductedTime) {
        this.reservationStatus = "DEDUCTED";
        this.deductedAt = deductedTime;
    }
}
