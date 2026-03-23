package com.github.thundax.bacon.upms.domain.entity;

import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
public class SysLogRecord {

    private Long id;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
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

    public SysLogRecord(Long id, String tenantId, String traceId, String requestId, String module,
                        String action, String eventType, String result, Long operatorId, String operatorName,
                        String clientIp, String requestUri, String httpMethod, Long costMs,
                        String errorMessage, Instant occurredAt) {
        this(id, null, null, null, null, tenantId, traceId, requestId, module, action, eventType, result,
                operatorId, operatorName, clientIp, requestUri, httpMethod, costMs, errorMessage, occurredAt);
    }

    public SysLogRecord(Long id, String createdBy, LocalDateTime createdAt, String updatedBy,
                        LocalDateTime updatedAt, String tenantId, String traceId, String requestId,
                        String module, String action, String eventType, String result, Long operatorId,
                        String operatorName, String clientIp, String requestUri, String httpMethod,
                        Long costMs, String errorMessage, Instant occurredAt) {
        this.id = id;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.requestId = requestId;
        this.module = module;
        this.action = action;
        this.eventType = eventType;
        this.result = result;
        this.operatorId = operatorId;
        this.operatorName = operatorName;
        this.clientIp = clientIp;
        this.requestUri = requestUri;
        this.httpMethod = httpMethod;
        this.costMs = costMs;
        this.errorMessage = errorMessage;
        this.occurredAt = occurredAt;
    }
}
