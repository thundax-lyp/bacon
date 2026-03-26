package com.github.thundax.bacon.inventory.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditReplayTaskDTO {

    private Long taskId;
    private Long tenantId;
    private String taskNo;
    private String status;
    private Integer totalCount;
    private Integer processedCount;
    private Integer successCount;
    private Integer failedCount;
    private String replayKeyPrefix;
    private Long operatorId;
    private String lastError;
    private Instant createdAt;
    private Instant startedAt;
    private Instant pausedAt;
    private Instant finishedAt;
    private Instant updatedAt;
}
