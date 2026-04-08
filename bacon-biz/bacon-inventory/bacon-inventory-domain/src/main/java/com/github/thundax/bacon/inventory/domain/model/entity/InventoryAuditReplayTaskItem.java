package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskItemStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
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

    /** 明细主键。 */
    private Long id;
    /** 回放任务主键。 */
    private TaskId taskId;
    /** 所属租户主键。 */
    private TenantId tenantId;
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

    public Long getTaskIdValue() {
        return taskId == null ? null : taskId.value();
    }

    public Long getDeadLetterIdValue() {
        return deadLetterId == null ? null : deadLetterId.value();
    }

    public String getItemStatusValue() {
        return itemStatus == null ? null : itemStatus.value();
    }

    public String getReplayStatusValue() {
        return replayStatus == null ? null : replayStatus.value();
    }
}
