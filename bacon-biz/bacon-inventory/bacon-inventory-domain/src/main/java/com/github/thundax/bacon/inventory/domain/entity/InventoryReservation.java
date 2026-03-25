package com.github.thundax.bacon.inventory.domain.entity;

import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
public class InventoryReservation {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_RESERVED = "RESERVED";
    public static final String STATUS_RELEASED = "RELEASED";
    public static final String STATUS_DEDUCTED = "DEDUCTED";
    public static final String STATUS_FAILED = "FAILED";

    private static final Set<String> RELEASE_REASONS = Set.of(
            "USER_CANCELLED",
            "SYSTEM_CANCELLED",
            "PAYMENT_CREATE_FAILED",
            "PAYMENT_FAILED",
            "TIMEOUT_CLOSED"
    );

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
        this.reservationStatus = STATUS_PROCESSING;
    }

    public static InventoryReservation rehydrate(Long id, Long tenantId, String reservationNo, String orderNo,
                                                 Long warehouseId, Instant createdAt, List<InventoryReservationItem> items,
                                                 String reservationStatus, String failureReason, String releaseReason,
                                                 Instant releasedAt, Instant deductedAt) {
        InventoryReservation reservation = new InventoryReservation(id, tenantId, reservationNo, orderNo, warehouseId,
                createdAt, items);
        reservation.reservationStatus = reservationStatus;
        reservation.failureReason = failureReason;
        reservation.releaseReason = releaseReason;
        reservation.releasedAt = releasedAt;
        reservation.deductedAt = deductedAt;
        return reservation;
    }

    public void reserve() {
        ensureStatus(STATUS_PROCESSING, STATUS_CREATED);
        this.reservationStatus = STATUS_RESERVED;
    }

    public void fail(String reason) {
        ensureStatus(STATUS_PROCESSING, STATUS_CREATED);
        this.reservationStatus = STATUS_FAILED;
        this.failureReason = reason;
    }

    public void release(String reason, Instant releasedTime) {
        ensureStatus(STATUS_RESERVED);
        if (!RELEASE_REASONS.contains(reason)) {
            throw new InventoryDomainException(InventoryErrorCode.INVALID_RELEASE_REASON, reason);
        }
        this.reservationStatus = STATUS_RELEASED;
        this.releaseReason = reason;
        this.releasedAt = releasedTime;
    }

    public void deduct(Instant deductedTime) {
        ensureStatus(STATUS_RESERVED);
        this.reservationStatus = STATUS_DEDUCTED;
        this.deductedAt = deductedTime;
    }

    public boolean isReserved() {
        return STATUS_RESERVED.equals(reservationStatus);
    }

    public boolean isProcessing() {
        return STATUS_PROCESSING.equals(reservationStatus) || STATUS_CREATED.equals(reservationStatus);
    }

    public boolean isReleased() {
        return STATUS_RELEASED.equals(reservationStatus);
    }

    public boolean isDeducted() {
        return STATUS_DEDUCTED.equals(reservationStatus);
    }

    public boolean isFailed() {
        return STATUS_FAILED.equals(reservationStatus);
    }

    private void ensureStatus(String... expectedStatuses) {
        for (String expectedStatus : expectedStatuses) {
            if (expectedStatus.equals(reservationStatus)) {
                return;
            }
        }
        throw new InventoryDomainException(InventoryErrorCode.INVALID_RESERVATION_STATUS, reservationStatus);
    }
}
