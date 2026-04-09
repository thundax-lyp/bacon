package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import java.util.List;

public final class InventoryReservationAssembler {

    private InventoryReservationAssembler() {
    }

    public static InventoryReservationDTO toDto(InventoryReservation reservation) {
        return new InventoryReservationDTO(
                reservation.getTenantId() == null ? null : reservation.getTenantId().value(),
                reservation.getOrderNoValue(),
                reservation.getReservationNoValue(),
                reservation.getReservationStatusValue(),
                reservation.getWarehouseNoValue(),
                toItemDtos(reservation.getItems()),
                reservation.getFailureReason(),
                reservation.getReleaseReasonValue(),
                reservation.getCreatedAt(),
                reservation.getReleasedAt(),
                reservation.getDeductedAt());
    }

    public static InventoryReservation toDomain(InventoryReservationDTO dto) {
        return InventoryReservation.rehydrate(
                null,
                dto.getTenantId(),
                dto.getReservationNo(),
                dto.getOrderNo(),
                dto.getWarehouseNo(),
                dto.getCreatedAt(),
                toDomainItems(dto.getTenantId(), dto.getReservationNo(), dto.getItems()),
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
                        item.getSkuId() == null ? null : item.getSkuId().value(),
                        item.getQuantity()))
                .toList();
    }

    public static List<InventoryReservationItem> toDomainItems(Long tenantId, String reservationNo,
                                                               List<InventoryReservationItemDTO> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        TenantId tenantIdValue = tenantId == null ? null : TenantId.of(tenantId);
        ReservationNo reservationNoValue = reservationNo == null ? null : ReservationNo.of(reservationNo);
        return items.stream()
                .map(item -> new InventoryReservationItem(
                        null,
                        tenantIdValue,
                        reservationNoValue,
                        item.getSkuId() == null ? null : SkuId.of(item.getSkuId()),
                        item.getQuantity()))
                .toList();
    }
}
