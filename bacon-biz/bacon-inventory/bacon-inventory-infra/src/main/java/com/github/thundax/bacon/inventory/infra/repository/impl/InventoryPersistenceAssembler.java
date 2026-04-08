package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryDO;

public final class InventoryPersistenceAssembler {

    private InventoryPersistenceAssembler() {
    }

    public static Inventory toDomain(InventoryDO dataObject) {
        return new Inventory(dataObject.getId(), dataObject.getTenantId(), dataObject.getSkuId(), dataObject.getWarehouseNo(),
                dataObject.getOnHandQuantity(), dataObject.getReservedQuantity(), dataObject.getAvailableQuantity(),
                InventoryStatus.from(dataObject.getStatus()), dataObject.getVersion(),
                dataObject.getUpdatedAt() == null ? dataObject.getCreatedAt() : dataObject.getUpdatedAt());
    }

    public static InventoryDO toDataObject(Inventory inventory) {
        return new InventoryDO(inventory.getId() == null ? null : inventory.getId().value(),
                inventory.getTenantId() == null ? null : inventory.getTenantId().value(),
                inventory.getSkuId() == null ? null : inventory.getSkuId().value(),
                inventory.getWarehouseNo() == null ? null : inventory.getWarehouseNo().value(),
                inventory.getOnHandQuantity(), inventory.getReservedQuantity(),
                inventory.getAvailableQuantity(), inventory.getStatus().value(), inventory.getVersion(), null,
                inventory.getUpdatedAt(), null,
                inventory.getUpdatedAt());
    }
}
