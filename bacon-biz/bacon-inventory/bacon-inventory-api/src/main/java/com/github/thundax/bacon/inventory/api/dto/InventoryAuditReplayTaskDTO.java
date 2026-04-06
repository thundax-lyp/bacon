package com.github.thundax.bacon.inventory.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存审计回放任务传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditReplayTaskDTO {

    /** 回放任务主键。 */
    private Long taskId;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 任务编号。 */
    private String taskNo;
    /** 任务状态。 */
    private String status;
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
    /** 操作人主键。 */
    private String operatorId;
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
}
