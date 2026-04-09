package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReservationStatus;

public final class InventoryReservationResultAssembler {

    private InventoryReservationResultAssembler() {
    }

    public static InventoryReservationResultDTO fromReservation(InventoryReservation reservation) {
        return new InventoryReservationResultDTO(reservation.getTenantId() == null ? null : reservation.getTenantId().value(), reservation.getOrderNoValue(),
                reservation.getReservationNoValue(), reservation.getReservationStatusValue(),
                toInventoryStatus(reservation.getReservationStatus()), reservation.getWarehouseNoValue(),
                reservation.getFailureReason(), reservation.getReleaseReasonValue(), reservation.getReleasedAt(),
                reservation.getDeductedAt());
    }

    public static InventoryReservationResultDTO failed(Long tenantId, String orderNo, String failureReason) {
        return new InventoryReservationResultDTO(tenantId, orderNo, null, InventoryReservationStatus.FAILED.value(),
                toInventoryStatus(InventoryReservationStatus.FAILED), null, failureReason, null, null, null);
    }

    public static String toInventoryStatus(InventoryReservationStatus reservationStatus) {
        return switch (reservationStatus) {
            case CREATED -> "RESERVING";
            case RESERVED -> "RESERVED";
            case RELEASED -> "RELEASED";
            case DEDUCTED -> "DEDUCTED";
            case FAILED -> "FAILED";
            default -> throw new InventoryDomainException(InventoryErrorCode.UNKNOWN_RESERVATION_STATUS,
                    reservationStatus.value());
        };
    }
}
