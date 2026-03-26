package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayTaskDTO;
import java.time.Instant;

public record InventoryAuditReplayTaskResponse(Long taskId,
                                               Long tenantId,
                                               String taskNo,
                                               String status,
                                               Integer totalCount,
                                               Integer processedCount,
                                               Integer successCount,
                                               Integer failedCount,
                                               String replayKeyPrefix,
                                               Long operatorId,
                                               String lastError,
                                               Instant createdAt,
                                               Instant startedAt,
                                               Instant pausedAt,
                                               Instant finishedAt,
                                               Instant updatedAt) {

    public static InventoryAuditReplayTaskResponse from(InventoryAuditReplayTaskDTO dto) {
        return new InventoryAuditReplayTaskResponse(dto.getTaskId(), dto.getTenantId(), dto.getTaskNo(),
                dto.getStatus(), dto.getTotalCount(), dto.getProcessedCount(), dto.getSuccessCount(),
                dto.getFailedCount(), dto.getReplayKeyPrefix(), dto.getOperatorId(), dto.getLastError(),
                dto.getCreatedAt(), dto.getStartedAt(), dto.getPausedAt(), dto.getFinishedAt(),
                dto.getUpdatedAt());
    }
}
