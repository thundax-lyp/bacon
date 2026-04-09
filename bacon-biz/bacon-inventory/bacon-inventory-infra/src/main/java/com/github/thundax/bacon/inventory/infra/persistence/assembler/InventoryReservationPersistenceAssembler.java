package com.github.thundax.bacon.inventory.infra.persistence.assembler;

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
                reservation.getTenantId(),
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
                reservation.getTenantId() == null
                        ? null
                        : reservation.getTenantId().value(),
                reservation.getReservationNoValue(),
                reservation.getOrderNoValue(),
                reservation.getReservationStatusValue(),
                reservation.getWarehouseCodeValue(),
                reservation.getFailureReason(),
                reservation.getReleaseReasonValue(),
                reservation.getCreatedAt(),
                reservation.getReleasedAt(),
                reservation.getDeductedAt());
    }
}
