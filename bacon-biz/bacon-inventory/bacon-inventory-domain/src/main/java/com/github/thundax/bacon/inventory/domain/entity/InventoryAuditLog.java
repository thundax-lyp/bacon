package com.github.thundax.bacon.inventory.domain.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InventoryAuditLog {

    public static final String ACTION_RESERVE = "RESERVE";
    public static final String ACTION_RESERVE_FAILED = "RESERVE_FAILED";
    public static final String ACTION_RELEASE = "RELEASE";
    public static final String ACTION_DEDUCT = "DEDUCT";

    public static final String OPERATOR_TYPE_SYSTEM = "SYSTEM";
    public static final Long OPERATOR_ID_SYSTEM = 0L;

    private Long id;
    private Long tenantId;
    private String orderNo;
    private String reservationNo;
    private String actionType;
    private String operatorType;
    private Long operatorId;
    private Instant occurredAt;
}
