package com.github.thundax.bacon.inventory.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存分页查询条件。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryPageQueryDTO {

    /** 所属租户主键。 */
    private Long tenantId;
    /** 商品 SKU 主键。 */
    private Long skuId;
    /** 库存状态。 */
    private String status;
    /** 页码。 */
    private Integer pageNo;
    /** 每页条数。 */
    private Integer pageSize;
}
