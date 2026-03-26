package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayResultDTO;

public record InventoryAuditReplayResultResponse(Long deadLetterId, String replayStatus, String replayKey,
                                                 String message) {

    public static InventoryAuditReplayResultResponse from(InventoryAuditReplayResultDTO dto) {
        return new InventoryAuditReplayResultResponse(dto.getDeadLetterId(), dto.getReplayStatus(), dto.getReplayKey(),
                dto.getMessage());
    }
}
