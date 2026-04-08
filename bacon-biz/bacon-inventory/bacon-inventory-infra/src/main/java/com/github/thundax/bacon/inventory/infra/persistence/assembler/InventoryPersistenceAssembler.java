package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.WarehouseNo;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryDO;

public final class InventoryPersistenceAssembler {

    private InventoryPersistenceAssembler() {
    }

    public static Inventory toDomain(InventoryDO dataObject) {
        return new Inventory(dataObject.getId() == null ? null : InventoryId.of(dataObject.getId()),
                dataObject.getTenantId() == null ? null : TenantId.of(dataObject.getTenantId()),
                dataObject.getSkuId() == null ? null : SkuId.of(dataObject.getSkuId()),
                dataObject.getWarehouseNo() == null ? null : WarehouseNo.of(dataObject.getWarehouseNo()),
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
