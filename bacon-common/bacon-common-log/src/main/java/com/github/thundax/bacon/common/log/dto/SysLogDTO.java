package com.github.thundax.bacon.common.log.dto;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.LogResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * Cross-module transport DTO for MQ-delivered system logs.
 */
@Getter
@AllArgsConstructor
public class SysLogDTO {

    private final String traceId;
    private final String requestId;
    private final String module;
    private final String action;
    private final LogEventType eventType;
    private final LogResult result;
    private final String operatorId;
    private final String operatorName;
    private final String tenantId;
    private final String clientIp;
    private final String requestUri;
    private final String httpMethod;
    private final String controllerClassName;
    private final String controllerMethodName;
    private final long costMs;
    private final String errorMessage;
    private final Instant occurredAt;
}
