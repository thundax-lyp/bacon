package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.UserId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统日志领域实体。
 */
@Getter
@AllArgsConstructor
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
    private UserId operatorId;
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
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public SysLogRecord(Long id, Long tenantId, String traceId, String requestId, String module,
                        String action, String eventType, String result, Long operatorId, String operatorName,
                        String clientIp, String requestUri, String httpMethod, Long costMs,
                        String errorMessage, Instant occurredAt, String createdBy, Instant createdAt,
                        String updatedBy, Instant updatedAt) {
        this(id, tenantId, traceId, requestId, module, action, eventType, result,
                operatorId == null ? null : UserId.of(operatorId), operatorName, clientIp, requestUri, httpMethod,
                costMs, errorMessage, occurredAt, createdBy, createdAt, updatedBy, updatedAt);
    }
}
