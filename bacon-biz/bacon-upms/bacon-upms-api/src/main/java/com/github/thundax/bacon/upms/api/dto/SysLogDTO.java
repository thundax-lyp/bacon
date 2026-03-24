package com.github.thundax.bacon.upms.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysLogDTO {

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
