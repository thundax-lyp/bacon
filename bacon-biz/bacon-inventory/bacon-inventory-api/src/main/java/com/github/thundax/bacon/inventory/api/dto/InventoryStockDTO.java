package com.github.thundax.bacon.inventory.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存主数据传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStockDTO {

    /** 所属租户主键。 */
    private Long tenantId;
    /** 商品 SKU 主键。 */
    private Long skuId;
    /** 仓库业务编码。 */
    private String warehouseCode;
    /** 在库数量。 */
    private Integer onHandQuantity;
    /** 预占数量。 */
    private Integer reservedQuantity;
    /** 可用数量。 */
    private Integer availableQuantity;
    /** 库存状态。 */
    private String status;
    /** 最后更新时间。 */
    private Instant updatedAt;
}
