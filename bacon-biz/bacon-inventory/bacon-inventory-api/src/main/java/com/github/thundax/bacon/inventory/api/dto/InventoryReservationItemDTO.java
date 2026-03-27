package com.github.thundax.bacon.inventory.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存预占明细传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationItemDTO {

    /** 商品 SKU 主键。 */
    @NotNull
    @Positive
    private Long skuId;
    /** 预占数量。 */
    @NotNull
    @Positive
    private Integer quantity;
}
