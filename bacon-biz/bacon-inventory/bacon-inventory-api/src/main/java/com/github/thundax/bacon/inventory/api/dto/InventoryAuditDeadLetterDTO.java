package com.github.thundax.bacon.inventory.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditDeadLetterDTO {

    private Long id;
    private Long outboxId;
    private Long tenantId;
    private String orderNo;
    private String reservationNo;
    private String actionType;
    private String operatorType;
    private Long operatorId;
    private Instant occurredAt;
    private Integer retryCount;
    private String errorMessage;
    private String deadReason;
    private Instant deadAt;
    private String replayStatus;
    private Integer replayCount;
    private Instant lastReplayAt;
    private String lastReplayResult;
    private String lastReplayError;
    private String replayKey;
    private String replayOperatorType;
    private Long replayOperatorId;
}
