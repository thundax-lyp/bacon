package com.github.thundax.bacon.order.domain.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderIdempotencyRecord {

    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    private Long id;
    private Long tenantId;
    private String orderNo;
    private String paymentNo;
    private String eventType;
    private String status;
    private Integer attemptCount;
    private String lastError;
    private Instant createdAt;
    private Instant updatedAt;
}
