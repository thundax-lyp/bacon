package com.github.thundax.bacon.inventory.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditLogDTO {

    private Long id;
    private Long tenantId;
    private String orderNo;
    private String reservationNo;
    private String actionType;
    private String operatorType;
    private Long operatorId;
    private Instant occurredAt;
}
