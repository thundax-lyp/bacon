package com.github.thundax.bacon.inventory.domain.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditOutbox {

    public static final String STATUS_NEW = "NEW";
    public static final String STATUS_RETRYING = "RETRYING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_DEAD = "DEAD";

    private Long id;
    private Long tenantId;
    private String orderNo;
    private String reservationNo;
    private String actionType;
    private String operatorType;
    private Long operatorId;
    private Instant occurredAt;
    private String errorMessage;
    private String status;
    private Integer retryCount;
    private Instant nextRetryAt;
    private String processingOwner;
    private Instant leaseUntil;
    private Instant claimedAt;
    private String deadReason;
    private Instant failedAt;
    private Instant updatedAt;
}
