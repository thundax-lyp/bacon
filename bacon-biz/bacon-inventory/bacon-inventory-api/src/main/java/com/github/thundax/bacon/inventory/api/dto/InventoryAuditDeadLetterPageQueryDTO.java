package com.github.thundax.bacon.inventory.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditDeadLetterPageQueryDTO {

    private Long tenantId;
    private String orderNo;
    private String replayStatus;
    private Integer pageNo;
    private Integer pageSize;
}
