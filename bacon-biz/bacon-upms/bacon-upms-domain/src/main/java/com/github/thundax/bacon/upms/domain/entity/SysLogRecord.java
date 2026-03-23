package com.github.thundax.bacon.upms.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class SysLogRecord {

    private Long id;
    private String tenantId;
    private String traceId;
    private String requestId;
    private String module;
    private String action;
    private String eventType;
    private String result;
    private Long operatorId;
    private String operatorName;
    private String clientIp;
    private String requestUri;
    private String httpMethod;
    private Long costMs;
    private String errorMessage;
    private Instant occurredAt;
}
