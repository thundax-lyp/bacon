package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryPageResultDTO;
import java.util.List;

public record InventoryPageResponse(List<InventoryStockResponse> records, long total, int pageNo, int pageSize) {

    public static InventoryPageResponse from(InventoryPageResultDTO dto) {
        List<InventoryStockResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(InventoryStockResponse::from).toList();
        return new InventoryPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
