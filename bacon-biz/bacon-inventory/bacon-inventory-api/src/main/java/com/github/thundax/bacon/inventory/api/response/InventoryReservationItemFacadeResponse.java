package com.github.thundax.bacon.inventory.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存预占明细门面响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationItemFacadeResponse {

    private Long skuId;

    private Integer quantity;
}
