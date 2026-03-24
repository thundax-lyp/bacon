package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.SysLogDTO;
import java.time.Instant;

public record SysLogResponse(Long id, String tenantId, String traceId, String requestId, String module, String action,
                             String eventType, String result, Long operatorId, String operatorName, String clientIp,
                             String requestUri, String httpMethod, Long costMs, String errorMessage,
                             Instant occurredAt) {

    public static SysLogResponse from(SysLogDTO dto) {
        return new SysLogResponse(dto.getId(), dto.getTenantId(), dto.getTraceId(), dto.getRequestId(),
                dto.getModule(), dto.getAction(), dto.getEventType(), dto.getResult(), dto.getOperatorId(),
                dto.getOperatorName(), dto.getClientIp(), dto.getRequestUri(), dto.getHttpMethod(),
                dto.getCostMs(), dto.getErrorMessage(), dto.getOccurredAt());
    }
}
