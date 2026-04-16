package com.github.thundax.bacon.inventory.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个库存查询门面请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAvailableStockFacadeRequest {

    @NotNull
    @Positive
    private Long skuId;
}
