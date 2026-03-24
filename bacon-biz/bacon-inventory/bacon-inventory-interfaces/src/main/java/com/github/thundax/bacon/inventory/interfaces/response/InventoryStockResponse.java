package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import java.time.Instant;

public record InventoryStockResponse(Long tenantId, Long skuId, Long warehouseId, Integer onHandQuantity,
                                     Integer reservedQuantity, Integer availableQuantity, String status,
                                     Instant updatedAt) {

    public static InventoryStockResponse from(InventoryStockDTO dto) {
        return new InventoryStockResponse(dto.getTenantId(), dto.getSkuId(), dto.getWarehouseId(),
                dto.getOnHandQuantity(), dto.getReservedQuantity(), dto.getAvailableQuantity(), dto.getStatus(),
                dto.getUpdatedAt());
    }
}
