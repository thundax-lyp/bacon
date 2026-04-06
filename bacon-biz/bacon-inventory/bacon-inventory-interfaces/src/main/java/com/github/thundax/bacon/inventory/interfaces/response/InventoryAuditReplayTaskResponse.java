package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayTaskDTO;
import java.time.Instant;

/**
 * 库存审计回放任务响应对象。
 */
public record InventoryAuditReplayTaskResponse(
        /** 回放任务主键。 */
        Long taskId,
        /** 所属租户主键。 */
        Long tenantId,
        /** 任务编号。 */
        String taskNo,
        /** 任务状态。 */
        String status,
        /** 总任务数。 */
        Integer totalCount,
        /** 已处理数量。 */
        Integer processedCount,
        /** 成功数量。 */
        Integer successCount,
        /** 失败数量。 */
        Integer failedCount,
        /** 回放幂等键前缀。 */
        String replayKeyPrefix,
        /** 操作人主键。 */
        String operatorId,
        /** 最近一次错误。 */
        String lastError,
        /** 创建时间。 */
        Instant createdAt,
        /** 开始时间。 */
        Instant startedAt,
        /** 暂停时间。 */
        Instant pausedAt,
        /** 完成时间。 */
        Instant finishedAt,
        /** 最后更新时间。 */
        Instant updatedAt) {

    public static InventoryAuditReplayTaskResponse from(InventoryAuditReplayTaskDTO dto) {
        return new InventoryAuditReplayTaskResponse(dto.getTaskId(), dto.getTenantId(), dto.getTaskNo(),
                dto.getStatus(), dto.getTotalCount(), dto.getProcessedCount(), dto.getSuccessCount(),
                dto.getFailedCount(), dto.getReplayKeyPrefix(), dto.getOperatorId(), dto.getLastError(),
                dto.getCreatedAt(), dto.getStartedAt(), dto.getPausedAt(), dto.getFinishedAt(),
                dto.getUpdatedAt());
    }
}
