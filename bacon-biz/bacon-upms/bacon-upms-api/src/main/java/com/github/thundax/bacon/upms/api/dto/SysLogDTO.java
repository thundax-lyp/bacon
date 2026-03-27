package com.github.thundax.bacon.upms.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * 系统日志跨服务传输对象。
 */
public class SysLogDTO {

    /** 日志主键。 */
    private Long id;
    /** 租户标识。 */
    private String tenantId;
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
    private Long operatorId;
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
}
