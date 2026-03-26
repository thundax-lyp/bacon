package com.github.thundax.bacon.inventory.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditReplayTaskCreateDTO {

    private Long tenantId;
    private Long operatorId;
    private String replayKeyPrefix;
    private List<Long> deadLetterIds;
}
