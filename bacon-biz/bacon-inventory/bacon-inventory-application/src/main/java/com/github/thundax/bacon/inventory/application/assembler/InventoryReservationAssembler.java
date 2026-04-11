package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.application.codec.ReservationNoCodec;
import com.github.thundax.bacon.inventory.application.codec.WarehouseCodeCodec;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import java.util.List;

public final class InventoryReservationAssembler {

    private InventoryReservationAssembler() {}

    public static InventoryReservationDTO toDto(InventoryReservation reservation) {
        return new InventoryReservationDTO(
                OrderNoCodec.toValue(reservation.getOrderNo()),
                ReservationNoCodec.toValue(reservation.getReservationNo()),
                reservation.getReservationStatus() == null
                        ? null
                        : reservation.getReservationStatus().value(),
                WarehouseCodeCodec.toValue(reservation.getWarehouseCode()),
                toItemDtos(reservation.getItems()),
                reservation.getFailureReason(),
                reservation.getReleaseReason() == null ? null : reservation.getReleaseReason().value(),
                reservation.getCreatedAt(),
                reservation.getReleasedAt(),
                reservation.getDeductedAt());
    }

    public static InventoryReservation toDomain(InventoryReservationDTO dto) {
        return InventoryReservation.rehydrate(
                null,
                dto.getReservationNo(),
                dto.getOrderNo(),
                dto.getWarehouseCode(),
                dto.getCreatedAt(),
                toDomainItems(dto.getReservationNo(), dto.getItems()),
                dto.getReservationStatus(),
                dto.getFailureReason(),
                dto.getReleaseReason(),
                dto.getReleasedAt(),
                dto.getDeductedAt());
    }

    public static List<InventoryReservationItemDTO> toItemDtos(List<InventoryReservationItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .map(item -> new InventoryReservationItemDTO(
                        item.getSkuId() == null ? null : item.getSkuId().value(), item.getQuantity()))
                .toList();
    }

    public static List<InventoryReservationItem> toDomainItems(
            String reservationNo, List<InventoryReservationItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        ReservationNo reservationNoValue = reservationNo == null ? null : ReservationNo.of(reservationNo);
        return items.stream()
                .map(item -> new InventoryReservationItem(
                        null,
                        reservationNoValue,
                        item.getSkuId() == null ? null : SkuId.of(item.getSkuId()),
                        item.getQuantity()))
                .toList();
    }
}
