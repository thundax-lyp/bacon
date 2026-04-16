package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.inventory.application.dto.InventoryAuditReplayTaskDTO;
import com.github.thundax.bacon.inventory.application.codec.TaskIdCodec;
import com.github.thundax.bacon.inventory.application.codec.TaskNoCodec;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;

public final class InventoryAuditReplayTaskAssembler {

    private InventoryAuditReplayTaskAssembler() {}

    public static InventoryAuditReplayTaskDTO toDto(InventoryAuditReplayTask task) {
        return new InventoryAuditReplayTaskDTO(
                TaskIdCodec.toValue(task.getId()),
                TaskNoCodec.toValue(task.getTaskNo()),
                task.getStatus().value(),
                task.getTotalCount(),
                task.getProcessedCount(),
                task.getSuccessCount(),
                task.getFailedCount(),
                task.getReplayKeyPrefix(),
                task.getOperatorId(),
                task.getLastError(),
                task.getCreatedAt(),
                task.getStartedAt(),
                task.getPausedAt(),
                task.getFinishedAt(),
                task.getUpdatedAt());
    }
}
