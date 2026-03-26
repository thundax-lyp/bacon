package com.github.thundax.bacon.order.domain.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderOutboxEvent {

    public static final String STATUS_NEW = "NEW";
    public static final String STATUS_RETRYING = "RETRYING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_DEAD = "DEAD";

    public static final String EVENT_RESERVE_STOCK = "RESERVE_STOCK";
    public static final String EVENT_CREATE_PAYMENT = "CREATE_PAYMENT";
    public static final String EVENT_RELEASE_STOCK = "RELEASE_STOCK";

    private Long id;
    private Long tenantId;
    private String orderNo;
    private String eventType;
    private String businessKey;
    private String payload;
    private String status;
    private Integer retryCount;
    private Instant nextRetryAt;
    private String processingOwner;
    private Instant leaseUntil;
    private Instant claimedAt;
    private String errorMessage;
    private String deadReason;
    private Instant createdAt;
    private Instant updatedAt;
}
