package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayResultDTO;

/**
 * 库存审计回放结果响应对象。
 */
public record InventoryAuditReplayResultResponse(
        /** 死信记录主键。 */
        Long deadLetterId,
        /** 回放状态。 */
        String replayStatus,
        /** 回放幂等键。 */
        String replayKey,
        /** 回放结果信息。 */
        String message) {

    public static InventoryAuditReplayResultResponse from(InventoryAuditReplayResultDTO dto) {
        return new InventoryAuditReplayResultResponse(dto.getDeadLetterId(), dto.getReplayStatus(), dto.getReplayKey(),
                dto.getMessage());
    }
}
