package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.inventory.application.codec.WarehouseCodeCodec;
import com.github.thundax.bacon.inventory.application.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;

public final class InventoryStockAssembler {

    private InventoryStockAssembler() {}

    public static InventoryStockDTO fromInventory(Inventory inventory) {
        return new InventoryStockDTO(
                inventory.getSkuId() == null ? null : inventory.getSkuId().value(),
                WarehouseCodeCodec.toValue(inventory.getWarehouseCode()),
                inventory.getOnHandQuantity() == null
                        ? null
                        : inventory.getOnHandQuantity().value(),
                inventory.getReservedQuantity() == null
                        ? null
                        : inventory.getReservedQuantity().value(),
                inventory.availableQuantity().value(),
                inventory.getStatus().value(),
                inventory.getUpdatedAt());
    }
}
