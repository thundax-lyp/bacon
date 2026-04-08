package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryReservationItemDO;

public final class InventoryReservationItemPersistenceAssembler {

    private InventoryReservationItemPersistenceAssembler() {
    }

    public static InventoryReservationItem toDomain(InventoryReservationItemDO item) {
        return new InventoryReservationItem(item.getId(), item.getTenantId(), item.getReservationNo(), item.getSkuId(),
                item.getQuantity());
    }

    public static InventoryReservationItemDO toDataObject(InventoryReservationItem item, Long tenantId, String reservationNo) {
        return new InventoryReservationItemDO(item.getId(), tenantId, reservationNo,
                item.getSkuId() == null ? null : item.getSkuId().value(), item.getQuantity());
    }
}
