package com.github.thundax.bacon.inventory.domain.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAuditDeadLetter {

    public static final String REPLAY_STATUS_PENDING = "PENDING";
    public static final String REPLAY_STATUS_RUNNING = "RUNNING";
    public static final String REPLAY_STATUS_SUCCEEDED = "SUCCEEDED";
    public static final String REPLAY_STATUS_FAILED = "FAILED";

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

    public InventoryAuditDeadLetter(Long id, Long outboxId, Long tenantId, String orderNo, String reservationNo,
                                    String actionType, String operatorType, Long operatorId, Instant occurredAt,
                                    Integer retryCount, String errorMessage, String deadReason, Instant deadAt) {
        this(id, outboxId, tenantId, orderNo, reservationNo, actionType, operatorType, operatorId, occurredAt,
                retryCount, errorMessage, deadReason, deadAt, REPLAY_STATUS_PENDING, 0, null, null, null, null,
                null, null);
    }
}
