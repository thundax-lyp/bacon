package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.application.codec.ReservationNoCodec;
import com.github.thundax.bacon.inventory.application.codec.WarehouseCodeCodec;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReservationStatus;

public final class InventoryReservationResultAssembler {

    private InventoryReservationResultAssembler() {}

    public static InventoryReservationResultDTO fromReservation(InventoryReservation reservation) {
        return new InventoryReservationResultDTO(
                OrderNoCodec.toValue(reservation.getOrderNo()),
                ReservationNoCodec.toValue(reservation.getReservationNo()),
                reservation.getReservationStatus() == null
                        ? null
                        : reservation.getReservationStatus().value(),
                toInventoryStatus(reservation.getReservationStatus()),
                WarehouseCodeCodec.toValue(reservation.getWarehouseCode()),
                reservation.getFailureReason(),
                reservation.getReleaseReason() == null
                        ? null
                        : reservation.getReleaseReason().value(),
                reservation.getReleasedAt(),
                reservation.getDeductedAt());
    }

    public static InventoryReservationResultDTO failed(String orderNo, String failureReason) {
        return new InventoryReservationResultDTO(
                orderNo,
                null,
                InventoryReservationStatus.FAILED.value(),
                toInventoryStatus(InventoryReservationStatus.FAILED),
                null,
                failureReason,
                null,
                null,
                null);
    }

    public static String toInventoryStatus(InventoryReservationStatus reservationStatus) {
        return switch (reservationStatus) {
            case CREATED -> "RESERVING";
            case RESERVED -> "RESERVED";
            case RELEASED -> "RELEASED";
            case DEDUCTED -> "DEDUCTED";
            case FAILED -> "FAILED";
            default ->
                throw new InventoryDomainException(
                        InventoryErrorCode.UNKNOWN_RESERVATION_STATUS, reservationStatus.value());
        };
    }
}
