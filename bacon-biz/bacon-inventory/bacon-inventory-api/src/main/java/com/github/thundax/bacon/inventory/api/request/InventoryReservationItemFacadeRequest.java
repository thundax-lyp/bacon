package com.github.thundax.bacon.inventory.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存预占门面请求明细。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationItemFacadeRequest {

    /** 商品 SKU 主键。 */
    @NotNull
    @Positive
    private Long skuId;
    /** 预占数量。 */
    @NotNull
    @Positive
    private Integer quantity;
}
