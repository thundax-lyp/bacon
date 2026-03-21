package com.github.thundax.bacon.inventory.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStockDTO {

    private Long tenantId;
    private Long skuId;
    private Long warehouseId;
    private Integer onHandQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private String status;
    private Instant updatedAt;
}
