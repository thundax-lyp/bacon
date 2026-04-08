package com.github.thundax.bacon.inventory.infra.persistence.assembler;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryAuditReplayTaskItemDO;

public final class InventoryAuditReplayTaskItemPersistenceAssembler {

    private InventoryAuditReplayTaskItemPersistenceAssembler() {
    }

    public static InventoryAuditReplayTaskItem toDomain(InventoryAuditReplayTaskItemDO dataObject) {
        return new InventoryAuditReplayTaskItem(dataObject.getId(), dataObject.getTaskId(),
                dataObject.getTenantId(),
                dataObject.getDeadLetterId(), dataObject.getItemStatus(), dataObject.getReplayStatus(),
                dataObject.getReplayKey(), dataObject.getResultMessage(), dataObject.getStartedAt(),
                dataObject.getFinishedAt(), dataObject.getUpdatedAt());
    }
}
