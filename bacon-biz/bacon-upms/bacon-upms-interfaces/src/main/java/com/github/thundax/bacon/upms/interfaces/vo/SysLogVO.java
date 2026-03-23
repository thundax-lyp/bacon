package com.github.thundax.bacon.upms.interfaces.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class SysLogVO {

    private Long id;
    private String tenantId;
    private String traceId;
    private String module;
    private String action;
    private String eventType;
    private String result;
    private Long operatorId;
    private String operatorName;
    private String requestUri;
    private String httpMethod;
    private Long costMs;
    private Instant occurredAt;
}
