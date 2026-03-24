package com.github.thundax.bacon.inventory.interfaces.response;

import java.time.Instant;

public record InventoryStockResponse(Long tenantId, Long skuId, Long warehouseId, Integer onHandQuantity,
                                     Integer reservedQuantity, Integer availableQuantity, String status,
                                     Instant updatedAt) {
}
