package com.github.thundax.bacon.inventory.api.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 批量库存门面响应。
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStockListFacadeResponse {

    private List<InventoryStockFacadeResponse> records;
}
