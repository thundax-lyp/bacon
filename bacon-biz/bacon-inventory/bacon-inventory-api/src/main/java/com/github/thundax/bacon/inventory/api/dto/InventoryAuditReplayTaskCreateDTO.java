package com.github.thundax.bacon.inventory.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存审计回放任务创建命令。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditReplayTaskCreateDTO {

    /** 操作人主键。 */
    private Long operatorId;
    /** 回放幂等键前缀。 */
    private String replayKeyPrefix;
    /** 待回放死信主键列表。 */
    private List<Long> deadLetterIds;
}
