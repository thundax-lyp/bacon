package com.github.thundax.bacon.inventory.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存审计死信分页查询条件。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditDeadLetterPageQueryDTO {

    /** 订单号。 */
    private String orderNo;
    /** 回放状态。 */
    private String replayStatus;
    /** 页码。 */
    private Integer pageNo;
    /** 每页条数。 */
    private Integer pageSize;
}
