package com.github.thundax.bacon.inventory.application.assembler;

import com.github.thundax.bacon.common.id.mapper.TenantIdMapper;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayTaskCreateDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayTaskDTO;
import com.github.thundax.bacon.inventory.application.codec.TaskIdCodec;
import com.github.thundax.bacon.inventory.application.codec.TaskNoCodec;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskNo;
import java.time.Instant;

public final class InventoryAuditReplayTaskAssembler {

    private InventoryAuditReplayTaskAssembler() {
    }

    public static InventoryAuditReplayTask toDomain(InventoryAuditReplayTaskCreateDTO createDTO, TaskNo taskNo,
                                                    String operatorType, Instant now) {
        return new InventoryAuditReplayTask(null,
                TenantIdMapper.toDomain(createDTO.getTenantId()),
                taskNo, InventoryAuditReplayTaskStatus.PENDING,
                createDTO.getDeadLetterIds().size(), 0, 0, 0, createDTO.getReplayKeyPrefix(), operatorType,
                createDTO.getOperatorId() == null ? null : String.valueOf(createDTO.getOperatorId()),
                null, null, null, now, null, null, null, now);
    }

    public static InventoryAuditReplayTaskDTO toDto(InventoryAuditReplayTask task) {
        return new InventoryAuditReplayTaskDTO(TaskIdCodec.toValue(task.getId()),
                TenantIdMapper.toValue(task.getTenantId()),
                TaskNoCodec.toValue(task.getTaskNo()),
                task.getStatus().value(),
                task.getTotalCount(), task.getProcessedCount(), task.getSuccessCount(), task.getFailedCount(),
                task.getReplayKeyPrefix(), task.getOperatorId(), task.getLastError(), task.getCreatedAt(),
                task.getStartedAt(), task.getPausedAt(), task.getFinishedAt(), task.getUpdatedAt());
    }
}
