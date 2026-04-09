package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.codec.WarehouseCodeCodec;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;

public final class InventoryStockAssembler {

    private InventoryStockAssembler() {}

    public static InventoryStockDTO fromInventory(Inventory inventory) {
        return new InventoryStockDTO(
                inventory.getTenantId() == null ? null : inventory.getTenantId().value(),
                inventory.getSkuId() == null ? null : inventory.getSkuId().value(),
                WarehouseCodeCodec.toValue(inventory.getWarehouseCode()),
                inventory.getOnHandQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity(),
                inventory.getStatus().value(),
                inventory.getUpdatedAt());
    }
}
