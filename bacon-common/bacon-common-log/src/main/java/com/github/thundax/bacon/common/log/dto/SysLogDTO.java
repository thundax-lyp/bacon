package com.github.thundax.bacon.common.log.dto;

import com.github.thundax.bacon.common.log.LogEventType;
import com.github.thundax.bacon.common.log.LogResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * 系统访问日志跨模块传输对象。
 */
@Getter
@AllArgsConstructor
public class SysLogDTO {

    /** 链路追踪标识。 */
    private final String traceId;
    /** 请求标识。 */
    private final String requestId;
    /** 业务模块名称。 */
    private final String module;
    /** 业务动作名称。 */
    private final String action;
    /** 日志事件类型。 */
    private final LogEventType eventType;
    /** 日志结果类型。 */
    private final LogResult result;
    /** 操作人标识。 */
    private final String operatorId;
    /** 操作人名称。 */
    private final String operatorName;
    /** 租户标识。 */
    private final String tenantId;
    /** 客户端 IP。 */
    private final String clientIp;
    /** 请求 URI。 */
    private final String requestUri;
    /** HTTP 方法。 */
    private final String httpMethod;
    /** 请求耗时，单位毫秒。 */
    private final long costMs;
    /** 错误信息。 */
    private final String errorMessage;
    /** 日志发生时间。 */
    private final Instant occurredAt;
}
