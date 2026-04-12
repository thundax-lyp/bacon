package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskNo;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 库存审计回放任务。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryAuditReplayTask {

    /** 回放任务主键。 */
    private TaskId id;
    /** 任务编号。 */
    private TaskNo taskNo;
    /** 任务状态。 */
    private InventoryAuditReplayTaskStatus status;
    /** 总任务数。 */
    private Integer totalCount;
    /** 已处理数量。 */
    private Integer processedCount;
    /** 成功数量。 */
    private Integer successCount;
    /** 失败数量。 */
    private Integer failedCount;
    /** 回放幂等键前缀。 */
    private String replayKeyPrefix;
    /** 操作人类型。 */
    private String operatorType;
    /** 操作人主键。 */
    private String operatorId;
    /** 当前处理节点标识。 */
    private String processingOwner;
    /** 租约到期时间。 */
    private Instant leaseUntil;
    /** 最近一次错误。 */
    private String lastError;
    /** 创建时间。 */
    private Instant createdAt;
    /** 开始时间。 */
    private Instant startedAt;
    /** 暂停时间。 */
    private Instant pausedAt;
    /** 完成时间。 */
    private Instant finishedAt;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public static InventoryAuditReplayTask create(
            TaskId id,
            TaskNo taskNo,
            InventoryAuditReplayTaskStatus status,
            Integer totalCount,
            Integer processedCount,
            Integer successCount,
            Integer failedCount,
            String replayKeyPrefix,
            String operatorType,
            String operatorId,
            String processingOwner,
            Instant leaseUntil,
            String lastError,
            Instant createdAt,
            Instant startedAt,
            Instant pausedAt,
            Instant finishedAt,
            Instant updatedAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(taskNo, "taskNo must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(totalCount, "totalCount must not be null");
        Objects.requireNonNull(processedCount, "processedCount must not be null");
        Objects.requireNonNull(successCount, "successCount must not be null");
        Objects.requireNonNull(failedCount, "failedCount must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        return new InventoryAuditReplayTask(
                id,
                taskNo,
                status,
                totalCount,
                processedCount,
                successCount,
                failedCount,
                replayKeyPrefix,
                operatorType,
                operatorId,
                processingOwner,
                leaseUntil,
                lastError,
                createdAt,
                startedAt,
                pausedAt,
                finishedAt,
                updatedAt);
    }

    public static InventoryAuditReplayTask reconstruct(
            TaskId id,
            TaskNo taskNo,
            InventoryAuditReplayTaskStatus status,
            Integer totalCount,
            Integer processedCount,
            Integer successCount,
            Integer failedCount,
            String replayKeyPrefix,
            String operatorType,
            String operatorId,
            String processingOwner,
            Instant leaseUntil,
            String lastError,
            Instant createdAt,
            Instant startedAt,
            Instant pausedAt,
            Instant finishedAt,
            Instant updatedAt) {
        return new InventoryAuditReplayTask(
                id,
                taskNo,
                status,
                totalCount,
                processedCount,
                successCount,
                failedCount,
                replayKeyPrefix,
                operatorType,
                operatorId,
                processingOwner,
                leaseUntil,
                lastError,
                createdAt,
                startedAt,
                pausedAt,
                finishedAt,
                updatedAt);
    }

    public void claim(String processingOwner, Instant leaseUntil, Instant updatedAt) {
        this.status = InventoryAuditReplayTaskStatus.RUNNING;
        this.processingOwner = processingOwner;
        this.leaseUntil = leaseUntil;
        if (this.startedAt == null) {
            this.startedAt = updatedAt;
        }
        this.updatedAt = updatedAt;
    }

    public void renewLease(Instant leaseUntil, Instant updatedAt) {
        this.leaseUntil = leaseUntil;
        this.updatedAt = updatedAt;
    }

    public void markItemProgress(int processedDelta, int successDelta, int failedDelta, Instant updatedAt) {
        this.processedCount = (processedCount == null ? 0 : processedCount) + Math.max(processedDelta, 0);
        this.successCount = (successCount == null ? 0 : successCount) + Math.max(successDelta, 0);
        this.failedCount = (failedCount == null ? 0 : failedCount) + Math.max(failedDelta, 0);
        this.updatedAt = updatedAt;
    }

    public void finish(InventoryAuditReplayTaskStatus status, String lastError, Instant finishedAt) {
        this.status = status;
        this.lastError = lastError;
        this.processingOwner = null;
        this.leaseUntil = null;
        this.finishedAt = finishedAt;
        this.updatedAt = finishedAt;
    }

    public void pause(Instant pausedAt) {
        this.status = InventoryAuditReplayTaskStatus.PAUSED;
        this.processingOwner = null;
        this.leaseUntil = null;
        this.pausedAt = pausedAt;
        this.updatedAt = pausedAt;
    }

    public void resume(Instant updatedAt) {
        this.status = InventoryAuditReplayTaskStatus.PENDING;
        this.pausedAt = null;
        this.updatedAt = updatedAt;
    }
}
