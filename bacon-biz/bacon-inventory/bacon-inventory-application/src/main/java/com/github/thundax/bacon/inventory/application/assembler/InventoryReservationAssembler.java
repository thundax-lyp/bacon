package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.application.codec.ReservationNoCodec;
import com.github.thundax.bacon.inventory.application.codec.WarehouseCodeCodec;
import com.github.thundax.bacon.inventory.application.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.application.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReleaseReason;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReservationStatus;
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
                reservation.getReleaseReason() == null
                        ? null
                        : reservation.getReleaseReason().value(),
                reservation.getCreatedAt(),
                reservation.getReleasedAt(),
                reservation.getDeductedAt());
    }

    public static InventoryReservation toDomain(InventoryReservationDTO dto) {
        return InventoryReservation.reconstruct(
                null,
                dto.getReservationNo() == null ? null : ReservationNo.of(dto.getReservationNo()),
                dto.getOrderNo() == null ? null : OrderNo.of(dto.getOrderNo()),
                dto.getWarehouseCode() == null ? null : WarehouseCode.of(dto.getWarehouseCode()),
                dto.getCreatedAt(),
                toDomainItems(dto.getReservationNo(), dto.getItems()),
                dto.getReservationStatus() == null ? null : InventoryReservationStatus.from(dto.getReservationStatus()),
                dto.getFailureReason(),
                dto.getReleaseReason() == null ? null : InventoryReleaseReason.from(dto.getReleaseReason()),
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
                .map(item -> InventoryReservationItem.reconstruct(
                        null,
                        reservationNoValue,
                        item.getSkuId() == null ? null : SkuId.of(item.getSkuId()),
                        item.getQuantity()))
                .toList();
    }
}
