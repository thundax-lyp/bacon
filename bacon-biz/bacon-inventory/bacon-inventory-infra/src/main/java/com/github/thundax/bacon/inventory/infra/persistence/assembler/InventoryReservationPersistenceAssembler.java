package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryReservationDO;
import java.util.List;

public final class InventoryReservationPersistenceAssembler {

    private InventoryReservationPersistenceAssembler() {}

    public static InventoryReservation toDomain(
            InventoryReservationDO reservation, List<InventoryReservationItem> items) {
        return InventoryReservation.rehydrate(
                reservation.getId(),
                reservation.getReservationNo(),
                reservation.getOrderNo(),
                reservation.getWarehouseCode(),
                reservation.getCreatedAt(),
                items,
                reservation.getReservationStatus(),
                reservation.getFailureReason(),
                reservation.getReleaseReason(),
                reservation.getReleasedAt(),
                reservation.getDeductedAt());
    }

    public static InventoryReservationDO toDataObject(InventoryReservation reservation) {
        return new InventoryReservationDO(
                reservation.getId(),
                BaconIdContextHelper.requireTenantId().value(),
                reservation.getReservationNo() == null ? null : reservation.getReservationNo().value(),
                reservation.getOrderNo() == null ? null : reservation.getOrderNo().value(),
                reservation.getReservationStatus() == null
                        ? null
                        : reservation.getReservationStatus().value(),
                reservation.getWarehouseCode() == null ? null : reservation.getWarehouseCode().value(),
                reservation.getFailureReason(),
                reservation.getReleaseReason() == null ? null : reservation.getReleaseReason().value(),
                reservation.getCreatedAt(),
                reservation.getReleasedAt(),
                reservation.getDeductedAt());
    }
}
