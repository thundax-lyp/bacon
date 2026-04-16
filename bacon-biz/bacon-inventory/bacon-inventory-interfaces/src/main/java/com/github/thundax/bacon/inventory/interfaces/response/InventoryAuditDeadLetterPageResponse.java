package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.application.result.InventoryAuditDeadLetterPageResult;
import java.util.List;

/**
 * 库存审计死信分页响应对象。
 */
public record InventoryAuditDeadLetterPageResponse(
        /** 当前页记录。 */
        List<InventoryAuditDeadLetterResponse> records,
        /** 总记录数。 */
        long total,
        /** 页码。 */
        int pageNo,
        /** 每页条数。 */
        int pageSize) {

    public static InventoryAuditDeadLetterPageResponse from(InventoryAuditDeadLetterPageResult dto) {
        List<InventoryAuditDeadLetterResponse> recordResponses = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream()
                        .map(InventoryAuditDeadLetterResponse::from)
                        .toList();
        return new InventoryAuditDeadLetterPageResponse(
                recordResponses, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }
}
