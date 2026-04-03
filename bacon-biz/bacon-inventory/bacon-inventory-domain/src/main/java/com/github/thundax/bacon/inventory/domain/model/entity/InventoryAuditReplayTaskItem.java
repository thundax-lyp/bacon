package com.github.thundax.bacon.inventory.domain.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存审计回放任务明细。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditReplayTaskItem {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCEEDED = "SUCCEEDED";
    public static final String STATUS_FAILED = "FAILED";

    /** 明细主键。 */
    private Long id;
    /** 回放任务主键。 */
    private String taskId;
    /** 所属租户主键。 */
    private String tenantId;
    /** 死信记录主键。 */
    private Long deadLetterId;
    /** 明细状态。 */
    private String itemStatus;
    /** 回放状态。 */
    private String replayStatus;
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

    public InventoryAuditReplayTaskItem(Long id, Long taskId, Long tenantId, Long deadLetterId, String itemStatus,
                                        String replayStatus, String replayKey, String resultMessage,
                                        Instant startedAt, Instant finishedAt, Instant updatedAt) {
        this(id, taskId == null ? null : String.valueOf(taskId), tenantId, deadLetterId, itemStatus, replayStatus,
                replayKey, resultMessage, startedAt, finishedAt, updatedAt);
    }

    public InventoryAuditReplayTaskItem(Long id, String taskId, Long tenantId, Long deadLetterId, String itemStatus,
                                        String replayStatus, String replayKey, String resultMessage,
                                        Instant startedAt, Instant finishedAt, Instant updatedAt) {
        this(id, taskId, tenantId == null ? null : String.valueOf(tenantId), deadLetterId, itemStatus, replayStatus,
                replayKey, resultMessage, startedAt, finishedAt, updatedAt);
    }
}
