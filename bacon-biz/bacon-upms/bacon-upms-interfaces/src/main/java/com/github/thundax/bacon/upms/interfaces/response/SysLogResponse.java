package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.SysLogDTO;
import java.time.Instant;

/**
 * 系统日志查询响应对象。
 */
public record SysLogResponse(
        /** 日志主键。 */
        Long id,
        /** 租户编号。 */
        String tenantNo,
        /** 链路追踪标识。 */
        String traceId,
        /** 请求标识。 */
        String requestId,
        /** 业务模块。 */
        String module,
        /** 操作名称。 */
        String action,
        /** 事件类型。 */
        String eventType,
        /** 执行结果。 */
        String result,
        /** 操作人主键。 */
        Long operatorId,
        /** 操作人名称。 */
        String operatorName,
        /** 客户端 IP。 */
        String clientIp,
        /** 请求 URI。 */
        String requestUri,
        /** HTTP 方法。 */
        String httpMethod,
        /** 耗时毫秒数。 */
        Long costMs,
        /** 错误信息。 */
        String errorMessage,
        /** 事件发生时间。 */
        Instant occurredAt) {

    public static SysLogResponse from(SysLogDTO dto) {
        return new SysLogResponse(dto.getId(), dto.getTenantNo(), dto.getTraceId(), dto.getRequestId(),
                dto.getModule(), dto.getAction(), dto.getEventType(), dto.getResult(), dto.getOperatorId(),
                dto.getOperatorName(), dto.getClientIp(), dto.getRequestUri(), dto.getHttpMethod(),
                dto.getCostMs(), dto.getErrorMessage(), dto.getOccurredAt());
    }
}
