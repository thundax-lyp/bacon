package com.github.thundax.bacon.inventory.domain.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditReplayTask {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_PAUSED = "PAUSED";
    public static final String STATUS_SUCCEEDED = "SUCCEEDED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CANCELED = "CANCELED";

    private Long id;
    private Long tenantId;
    private String taskNo;
    private String status;
    private Integer totalCount;
    private Integer processedCount;
    private Integer successCount;
    private Integer failedCount;
    private String replayKeyPrefix;
    private String operatorType;
    private Long operatorId;
    private String processingOwner;
    private Instant leaseUntil;
    private String lastError;
    private Instant createdAt;
    private Instant startedAt;
    private Instant pausedAt;
    private Instant finishedAt;
    private Instant updatedAt;
}
