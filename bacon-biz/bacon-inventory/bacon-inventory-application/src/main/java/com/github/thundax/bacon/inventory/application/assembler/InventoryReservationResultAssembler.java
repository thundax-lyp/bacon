package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;

public final class InventoryReservationResultAssembler {

    private InventoryReservationResultAssembler() {
    }

    public static InventoryReservationResultDTO fromReservation(InventoryReservation reservation) {
        return new InventoryReservationResultDTO(reservation.getTenantId(), reservation.getOrderNo(),
                reservation.getReservationNo(), reservation.getReservationStatus(),
                toInventoryStatus(reservation.getReservationStatus()), reservation.getWarehouseId(),
                reservation.getFailureReason(), reservation.getReleaseReason(), reservation.getReleasedAt(),
                reservation.getDeductedAt());
    }

    public static InventoryReservationResultDTO failed(Long tenantId, String orderNo, String failureReason) {
        return new InventoryReservationResultDTO(tenantId, orderNo, null, InventoryReservation.STATUS_FAILED,
                toInventoryStatus(InventoryReservation.STATUS_FAILED), null, failureReason, null, null, null);
    }

    public static String toInventoryStatus(String reservationStatus) {
        return switch (reservationStatus) {
            case InventoryReservation.STATUS_CREATED -> "RESERVING";
            case InventoryReservation.STATUS_RESERVED -> "RESERVED";
            case InventoryReservation.STATUS_RELEASED -> "RELEASED";
            case InventoryReservation.STATUS_DEDUCTED -> "DEDUCTED";
            case InventoryReservation.STATUS_FAILED -> "FAILED";
            default -> throw new InventoryDomainException(InventoryErrorCode.UNKNOWN_RESERVATION_STATUS, reservationStatus);
        };
    }
}
