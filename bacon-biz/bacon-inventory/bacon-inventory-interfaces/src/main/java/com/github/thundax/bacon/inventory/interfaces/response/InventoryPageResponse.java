package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.common.application.page.PageResult;
import com.github.thundax.bacon.inventory.application.dto.InventoryStockDTO;
import java.util.List;

/**
 * 库存分页响应对象。
 */
public record InventoryPageResponse(
        /** 当前页记录。 */
        List<InventoryStockResponse> records,
        /** 总记录数。 */
        long total,
        /** 页码。 */
        int pageNo,
        /** 每页条数。 */
        int pageSize) {

    public static InventoryPageResponse from(PageResult<InventoryStockDTO> dto) {
        List<InventoryStockResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(InventoryStockResponse::from).toList();
        return new InventoryPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
