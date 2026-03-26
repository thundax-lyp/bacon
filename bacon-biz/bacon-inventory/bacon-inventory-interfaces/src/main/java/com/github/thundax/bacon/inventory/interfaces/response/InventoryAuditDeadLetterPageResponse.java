package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryAuditDeadLetterPageResultDTO;
import java.util.List;

public record InventoryAuditDeadLetterPageResponse(List<InventoryAuditDeadLetterResponse> records,
                                                   long total,
                                                   int pageNo,
                                                   int pageSize) {

    public static InventoryAuditDeadLetterPageResponse from(InventoryAuditDeadLetterPageResultDTO dto) {
        List<InventoryAuditDeadLetterResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(InventoryAuditDeadLetterResponse::from).toList();
        return new InventoryAuditDeadLetterPageResponse(recordResponses, dto.getTotal(), dto.getPageNo(),
                dto.getPageSize());
    }
}
