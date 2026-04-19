package com.github.thundax.bacon.upms.application.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统日志应用层读模型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysLogDTO {

    private Long id;
    private String traceId;
    private String requestId;
    private String module;
    private String action;
    private String eventType;
    private String result;
    private String operatorId;
    private String operatorName;
    private String clientIp;
    private String requestUri;
    private String httpMethod;
    private Long costMs;
    private String errorMessage;
    private Instant occurredAt;
}
