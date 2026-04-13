package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskItemStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 库存审计回放任务明细。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryAuditReplayTaskItem {

    /** 明细主键。 */
    private Long id;
    /** 回放任务主键。 */
    private TaskId taskId;
    /** 死信记录主键。 */
    private DeadLetterId deadLetterId;
    /** 明细状态。 */
    private InventoryAuditReplayTaskItemStatus itemStatus;
    /** 回放状态。 */
    private InventoryAuditReplayStatus replayStatus;
    /** 回放幂等键。 */
    private String replayKey;
    /** 结果信息。 */
    private String resultMessage;
    /** 开始时间。 */
    private Instant startedAt;
    /** 完成时间。 */
    private Instant finishedAt;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public static InventoryAuditReplayTaskItem create(
            Long id, TaskId taskId, DeadLetterId deadLetterId, Instant updatedAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(taskId, "taskId must not be null");
        Objects.requireNonNull(deadLetterId, "deadLetterId must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        return new InventoryAuditReplayTaskItem(
                id,
                taskId,
                deadLetterId,
                InventoryAuditReplayTaskItemStatus.PENDING,
                null,
                null,
                null,
                null,
                null,
                updatedAt);
    }

    public static InventoryAuditReplayTaskItem reconstruct(
            Long id,
            TaskId taskId,
            DeadLetterId deadLetterId,
            InventoryAuditReplayTaskItemStatus itemStatus,
            InventoryAuditReplayStatus replayStatus,
            String replayKey,
            String resultMessage,
            Instant startedAt,
            Instant finishedAt,
            Instant updatedAt) {
        return new InventoryAuditReplayTaskItem(
                id,
                taskId,
                deadLetterId,
                itemStatus,
                replayStatus,
                replayKey,
                resultMessage,
                startedAt,
                finishedAt,
                updatedAt);
    }

    public void markResult(
            InventoryAuditReplayTaskItemStatus itemStatus,
            InventoryAuditReplayStatus replayStatus,
            String replayKey,
            String resultMessage,
            Instant startedAt,
            Instant finishedAt) {
        this.itemStatus = itemStatus;
        this.replayStatus = replayStatus;
        this.replayKey = replayKey;
        this.resultMessage = resultMessage;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.updatedAt = finishedAt;
    }
}
