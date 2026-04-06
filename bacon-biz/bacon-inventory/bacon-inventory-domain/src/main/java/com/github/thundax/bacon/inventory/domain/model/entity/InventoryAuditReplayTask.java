package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存审计回放任务。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditReplayTask {

    public static final InventoryAuditReplayTaskStatus STATUS_PENDING = InventoryAuditReplayTaskStatus.PENDING;
    public static final InventoryAuditReplayTaskStatus STATUS_RUNNING = InventoryAuditReplayTaskStatus.RUNNING;
    public static final InventoryAuditReplayTaskStatus STATUS_PAUSED = InventoryAuditReplayTaskStatus.PAUSED;
    public static final InventoryAuditReplayTaskStatus STATUS_SUCCEEDED = InventoryAuditReplayTaskStatus.SUCCEEDED;
    public static final InventoryAuditReplayTaskStatus STATUS_FAILED = InventoryAuditReplayTaskStatus.FAILED;
    public static final InventoryAuditReplayTaskStatus STATUS_CANCELED = InventoryAuditReplayTaskStatus.CANCELED;

    /** 回放任务主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 任务编号。 */
    private String taskNo;
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

    public InventoryAuditReplayTask(Long id, Long tenantId, String taskNo, String status, Integer totalCount,
                                    Integer processedCount, Integer successCount, Integer failedCount,
                                    String replayKeyPrefix, String operatorType, Long operatorId,
                                    String processingOwner, Instant leaseUntil, String lastError, Instant createdAt,
                                    Instant startedAt, Instant pausedAt, Instant finishedAt, Instant updatedAt) {
        this(id, tenantId, taskNo,
                status == null ? null : InventoryAuditReplayTaskStatus.fromValue(status), totalCount, processedCount,
                successCount, failedCount, replayKeyPrefix, operatorType,
                operatorId == null ? null : String.valueOf(operatorId), processingOwner, leaseUntil, lastError,
                createdAt, startedAt, pausedAt, finishedAt, updatedAt);
    }

    public InventoryAuditReplayTask(Long id, Long tenantId, String taskNo, InventoryAuditReplayTaskStatus status,
                                    Integer totalCount, Integer processedCount, Integer successCount,
                                    Integer failedCount, String replayKeyPrefix, String operatorType, Long operatorId,
                                    String processingOwner, Instant leaseUntil, String lastError, Instant createdAt,
                                    Instant startedAt, Instant pausedAt, Instant finishedAt, Instant updatedAt) {
        this(id, tenantId, taskNo, status, totalCount, processedCount,
                successCount, failedCount, replayKeyPrefix, operatorType,
                operatorId == null ? null : String.valueOf(operatorId), processingOwner, leaseUntil, lastError,
                createdAt, startedAt, pausedAt, finishedAt, updatedAt);
    }

    public Long getOperatorIdValue() {
        return operatorId == null ? null : Long.valueOf(operatorId);
    }
}
