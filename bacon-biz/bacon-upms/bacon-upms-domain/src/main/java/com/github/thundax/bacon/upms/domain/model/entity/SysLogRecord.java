package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.OperatorId;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 系统日志领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SysLogRecord {

    /** 日志主键。 */
    private Long id;
    /** 租户标识。 */
    private Long tenantId;
    /** 链路追踪标识。 */
    private String traceId;
    /** 请求标识。 */
    private String requestId;
    /** 业务模块。 */
    private String module;
    /** 操作名称。 */
    private String action;
    /** 事件类型。 */
    private String eventType;
    /** 执行结果。 */
    private String result;
    /** 操作人主键。 */
    private OperatorId operatorId;
    /** 操作人名称。 */
    private String operatorName;
    /** 客户端 IP。 */
    private String clientIp;
    /** 请求 URI。 */
    private String requestUri;
    /** HTTP 方法。 */
    private String httpMethod;
    /** 耗时毫秒数。 */
    private Long costMs;
    /** 错误信息。 */
    private String errorMessage;
    /** 事件发生时间。 */
    private Instant occurredAt;

    public static SysLogRecord create(
            Long id,
            Long tenantId,
            String traceId,
            String requestId,
            String module,
            String action,
            String eventType,
            String result,
            OperatorId operatorId,
            String operatorName,
            String clientIp,
            String requestUri,
            String httpMethod,
            Long costMs,
            String errorMessage,
            Instant occurredAt) {
        Objects.requireNonNull(id, "id must not be null");
        return new SysLogRecord(
                id,
                tenantId,
                traceId,
                requestId,
                module,
                action,
                eventType,
                result,
                operatorId,
                operatorName,
                clientIp,
                requestUri,
                httpMethod,
                costMs,
                errorMessage,
                occurredAt);
    }

    public static SysLogRecord reconstruct(
            Long id,
            Long tenantId,
            String traceId,
            String requestId,
            String module,
            String action,
            String eventType,
            String result,
            OperatorId operatorId,
            String operatorName,
            String clientIp,
            String requestUri,
            String httpMethod,
            Long costMs,
            String errorMessage,
            Instant occurredAt) {
        return new SysLogRecord(
                id,
                tenantId,
                traceId,
                requestId,
                module,
                action,
                eventType,
                result,
                operatorId,
                operatorName,
                clientIp,
                requestUri,
                httpMethod,
                costMs,
                errorMessage,
                occurredAt);
    }
}
