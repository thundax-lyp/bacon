package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskItemStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditReplayTaskItemDO;

public final class InventoryAuditReplayTaskItemPersistenceAssembler {

    private InventoryAuditReplayTaskItemPersistenceAssembler() {}

    public static InventoryAuditReplayTaskItemDO toDataObject(InventoryAuditReplayTaskItem item) {
        return new InventoryAuditReplayTaskItemDO(
                item.getId(),
                item.getTaskId() == null ? null : item.getTaskId().value(),
                BaconContextHolder.requireTenantId(),
                item.getDeadLetterId() == null ? null : item.getDeadLetterId().value(),
                item.getItemStatus() == null ? null : item.getItemStatus().value(),
                item.getReplayStatus() == null ? null : item.getReplayStatus().value(),
                item.getReplayKey(),
                item.getResultMessage(),
                item.getStartedAt(),
                item.getFinishedAt(),
                item.getUpdatedAt());
    }

    public static InventoryAuditReplayTaskItem toDomain(InventoryAuditReplayTaskItemDO dataObject) {
        return InventoryAuditReplayTaskItem.reconstruct(
                dataObject.getId(),
                dataObject.getTaskId() == null ? null : TaskId.of(dataObject.getTaskId()),
                dataObject.getDeadLetterId() == null ? null : DeadLetterId.of(dataObject.getDeadLetterId()),
                dataObject.getItemStatus() == null
                        ? null
                        : InventoryAuditReplayTaskItemStatus.from(dataObject.getItemStatus()),
                dataObject.getReplayStatus() == null
                        ? null
                        : InventoryAuditReplayStatus.from(dataObject.getReplayStatus()),
                dataObject.getReplayKey(),
                dataObject.getResultMessage(),
                dataObject.getStartedAt(),
                dataObject.getFinishedAt(),
                dataObject.getUpdatedAt());
    }
}
