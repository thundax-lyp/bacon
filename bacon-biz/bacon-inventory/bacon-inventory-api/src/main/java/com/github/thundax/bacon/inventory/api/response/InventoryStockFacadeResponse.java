package com.github.thundax.bacon.inventory.api.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存门面响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStockFacadeResponse {

    private Long skuId;

    private String warehouseCode;

    private Integer onHandQuantity;

    private Integer reservedQuantity;

    private Integer availableQuantity;

    private String status;

    private Instant updatedAt;
}
