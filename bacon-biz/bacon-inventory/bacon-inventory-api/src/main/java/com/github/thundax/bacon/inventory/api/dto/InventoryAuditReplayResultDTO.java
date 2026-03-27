package com.github.thundax.bacon.inventory.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存审计回放结果传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditReplayResultDTO {

    /** 死信记录主键。 */
    private Long deadLetterId;
    /** 回放状态。 */
    private String replayStatus;
    /** 回放幂等键。 */
    private String replayKey;
    /** 回放结果信息。 */
    private String message;
}
