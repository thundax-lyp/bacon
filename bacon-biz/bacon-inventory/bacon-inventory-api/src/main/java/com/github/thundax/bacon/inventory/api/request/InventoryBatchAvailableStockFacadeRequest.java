package com.github.thundax.bacon.inventory.api.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 批量库存查询门面请求。
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBatchAvailableStockFacadeRequest {

    @NotNull
    private Set<@NotNull @Positive Long> skuIds;
}
