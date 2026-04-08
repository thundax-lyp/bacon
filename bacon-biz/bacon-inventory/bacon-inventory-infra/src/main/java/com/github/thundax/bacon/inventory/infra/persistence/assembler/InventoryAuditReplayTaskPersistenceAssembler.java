package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskStatus;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditReplayTaskDO;

public final class InventoryAuditReplayTaskPersistenceAssembler {

    private InventoryAuditReplayTaskPersistenceAssembler() {
    }

    public static InventoryAuditReplayTaskDO toDataObject(InventoryAuditReplayTask task) {
        return new InventoryAuditReplayTaskDO(task.getIdValue(), task.getTenantId() == null ? null : task.getTenantId().value(),
                task.getTaskNoValue(), task.getStatus().value(),
                task.getTotalCount(), task.getProcessedCount(), task.getSuccessCount(), task.getFailedCount(),
                task.getReplayKeyPrefix(), task.getOperatorType(), task.getOperatorIdValue(), task.getProcessingOwner(),
                task.getLeaseUntil(), task.getLastError(), task.getCreatedAt(), task.getStartedAt(), task.getPausedAt(),
                task.getFinishedAt(), task.getUpdatedAt());
    }

    public static InventoryAuditReplayTask toDomain(InventoryAuditReplayTaskDO dataObject) {
        return new InventoryAuditReplayTask(dataObject.getId(), dataObject.getTenantId(), dataObject.getTaskNo(),
                InventoryAuditReplayTaskStatus.from(dataObject.getStatus()), dataObject.getTotalCount(), dataObject.getProcessedCount(),
                dataObject.getSuccessCount(), dataObject.getFailedCount(), dataObject.getReplayKeyPrefix(),
                dataObject.getOperatorType(), dataObject.getOperatorId(), dataObject.getProcessingOwner(),
                dataObject.getLeaseUntil(), dataObject.getLastError(), dataObject.getCreatedAt(),
                dataObject.getStartedAt(), dataObject.getPausedAt(), dataObject.getFinishedAt(),
                dataObject.getUpdatedAt());
    }
}
