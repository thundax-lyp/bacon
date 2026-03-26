package com.github.thundax.bacon.order.domain.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderOutboxDeadLetter {

    public static final String REPLAY_STATUS_PENDING = "PENDING";
    public static final String REPLAY_STATUS_SUCCESS = "SUCCESS";
    public static final String REPLAY_STATUS_FAILED = "FAILED";

    private Long id;
    private Long outboxId;
    private Long tenantId;
    private String orderNo;
    private String eventType;
    private String businessKey;
    private String payload;
    private Integer retryCount;
    private String errorMessage;
    private String deadReason;
    private Instant deadAt;
    private String replayStatus;
    private Integer replayCount;
    private Instant lastReplayAt;
    private String lastReplayMessage;
    private Instant createdAt;
    private Instant updatedAt;
}
