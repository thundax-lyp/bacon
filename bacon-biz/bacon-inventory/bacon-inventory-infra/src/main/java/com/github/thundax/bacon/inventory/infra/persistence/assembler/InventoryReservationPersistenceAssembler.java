package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReleaseReason;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReservationStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryReservationDO;
import java.util.List;

public final class InventoryReservationPersistenceAssembler {

    private InventoryReservationPersistenceAssembler() {}

    public static InventoryReservation toDomain(
            InventoryReservationDO reservation, List<InventoryReservationItem> items) {
        return InventoryReservation.reconstruct(
                reservation.getId(),
                reservation.getReservationNo() == null
                        ? null
                        : ReservationNo.of(reservation.getReservationNo()),
                reservation.getOrderNo() == null
                        ? null
                        : OrderNo.of(reservation.getOrderNo()),
                reservation.getWarehouseCode() == null
                        ? null
                        : WarehouseCode.of(reservation.getWarehouseCode()),
                reservation.getCreatedAt(),
                items,
                reservation.getReservationStatus() == null
                        ? null
                        : InventoryReservationStatus.from(reservation.getReservationStatus()),
                reservation.getFailureReason(),
                reservation.getReleaseReason() == null
                        ? null
                        : InventoryReleaseReason.from(reservation.getReleaseReason()),
                reservation.getReleasedAt(),
                reservation.getDeductedAt());
    }

    public static InventoryReservationDO toDataObject(InventoryReservation reservation) {
        return new InventoryReservationDO(
                reservation.getId(),
                BaconContextHolder.requireTenantId(),
                reservation.getReservationNo() == null
                        ? null
                        : reservation.getReservationNo().value(),
                reservation.getOrderNo() == null
                        ? null
                        : reservation.getOrderNo().value(),
                reservation.getReservationStatus() == null
                        ? null
                        : reservation.getReservationStatus().value(),
                reservation.getWarehouseCode() == null
                        ? null
                        : reservation.getWarehouseCode().value(),
                reservation.getFailureReason(),
                reservation.getReleaseReason() == null
                        ? null
                        : reservation.getReleaseReason().value(),
                reservation.getCreatedAt(),
                reservation.getReleasedAt(),
                reservation.getDeductedAt());
    }
}
