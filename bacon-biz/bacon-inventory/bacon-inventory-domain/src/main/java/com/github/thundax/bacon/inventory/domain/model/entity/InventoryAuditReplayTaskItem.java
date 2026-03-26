package com.github.thundax.bacon.inventory.domain.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditReplayTaskItem {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCEEDED = "SUCCEEDED";
    public static final String STATUS_FAILED = "FAILED";

    private Long id;
    private Long taskId;
    private Long tenantId;
    private Long deadLetterId;
    private String itemStatus;
    private String replayStatus;
    private String replayKey;
    private String resultMessage;
    private Instant startedAt;
    private Instant finishedAt;
    private Instant updatedAt;
}
