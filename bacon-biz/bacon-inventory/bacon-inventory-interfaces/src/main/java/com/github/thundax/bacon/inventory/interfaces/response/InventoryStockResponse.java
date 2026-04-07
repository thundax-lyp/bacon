package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import java.time.Instant;

/**
 * 库存主数据响应对象。
 */
public record InventoryStockResponse(
        /** 所属租户主键。 */
        Long tenantId,
        /** 商品 SKU 主键。 */
        Long skuId,
        /** 仓库业务编号。 */
        String warehouseNo,
        /** 在库数量。 */
        Integer onHandQuantity,
        /** 预占数量。 */
        Integer reservedQuantity,
        /** 可用数量。 */
        Integer availableQuantity,
        /** 库存状态。 */
        String status,
        /** 最后更新时间。 */
        Instant updatedAt) {

    public static InventoryStockResponse from(InventoryStockDTO dto) {
        return new InventoryStockResponse(dto.getTenantId(), dto.getSkuId(), dto.getWarehouseNo(),
                dto.getOnHandQuantity(), dto.getReservedQuantity(), dto.getAvailableQuantity(), dto.getStatus(),
                dto.getUpdatedAt());
    }
}
