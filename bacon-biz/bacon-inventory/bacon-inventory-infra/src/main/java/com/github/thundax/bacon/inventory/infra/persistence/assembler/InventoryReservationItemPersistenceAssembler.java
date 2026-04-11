package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryReservationItemDO;

public final class InventoryReservationItemPersistenceAssembler {

    private InventoryReservationItemPersistenceAssembler() {}

    public static InventoryReservationItem toDomain(InventoryReservationItemDO item) {
        return new InventoryReservationItem(
                item.getId(),
                item.getReservationNo() == null ? null : ReservationNo.of(item.getReservationNo()),
                item.getSkuId() == null ? null : SkuId.of(item.getSkuId()),
                item.getQuantity());
    }

    public static InventoryReservationItemDO toDataObject(
            InventoryReservationItem item, Long tenantId, String reservationNo) {
        return new InventoryReservationItemDO(
                item.getId(),
                tenantId,
                reservationNo,
                item.getSkuId() == null ? null : item.getSkuId().value(),
                item.getQuantity());
    }
}
