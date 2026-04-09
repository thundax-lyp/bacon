package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskNo;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditReplayTaskDO;

public final class InventoryAuditReplayTaskPersistenceAssembler {

    private InventoryAuditReplayTaskPersistenceAssembler() {}

    public static InventoryAuditReplayTaskDO toDataObject(InventoryAuditReplayTask task) {
        return new InventoryAuditReplayTaskDO(
                task.getIdValue(),
                task.getTenantId() == null ? null : task.getTenantId().value(),
                task.getTaskNoValue(),
                task.getStatus().value(),
                task.getTotalCount(),
                task.getProcessedCount(),
                task.getSuccessCount(),
                task.getFailedCount(),
                task.getReplayKeyPrefix(),
                task.getOperatorType(),
                task.getOperatorIdValue(),
                task.getProcessingOwner(),
                task.getLeaseUntil(),
                task.getLastError(),
                task.getCreatedAt(),
                task.getStartedAt(),
                task.getPausedAt(),
                task.getFinishedAt(),
                task.getUpdatedAt());
    }

    public static InventoryAuditReplayTask toDomain(InventoryAuditReplayTaskDO dataObject) {
        return new InventoryAuditReplayTask(
                dataObject.getId() == null ? null : TaskId.of(dataObject.getId()),
                dataObject.getTenantId() == null ? null : TenantId.of(dataObject.getTenantId()),
                dataObject.getTaskNo() == null ? null : TaskNo.of(dataObject.getTaskNo()),
                InventoryAuditReplayTaskStatus.from(dataObject.getStatus()),
                dataObject.getTotalCount(),
                dataObject.getProcessedCount(),
                dataObject.getSuccessCount(),
                dataObject.getFailedCount(),
                dataObject.getReplayKeyPrefix(),
                dataObject.getOperatorType(),
                dataObject.getOperatorId() == null ? null : String.valueOf(dataObject.getOperatorId()),
                dataObject.getProcessingOwner(),
                dataObject.getLeaseUntil(),
                dataObject.getLastError(),
                dataObject.getCreatedAt(),
                dataObject.getStartedAt(),
                dataObject.getPausedAt(),
                dataObject.getFinishedAt(),
                dataObject.getUpdatedAt());
    }
}
