package com.github.thundax.bacon.inventory.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditReplayResultDTO {

    private Long deadLetterId;
    private String replayStatus;
    private String replayKey;
    private String message;
}
