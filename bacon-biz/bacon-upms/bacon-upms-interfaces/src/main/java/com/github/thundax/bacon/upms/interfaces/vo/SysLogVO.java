package com.github.thundax.bacon.upms.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Schema(description = "系统访问日志视图对象")
public class SysLogVO {

    @Schema(description = "日志ID", example = "1")
    private Long id;

    @Schema(description = "租户业务键", example = "1001")
    private String tenantId;

    @Schema(description = "链路ID", example = "trace-001")
    private String traceId;

    @Schema(description = "业务模块", example = "UPMS")
    private String module;

    @Schema(description = "操作描述", example = "查询用户详情")
    private String action;

    @Schema(description = "事件类型", example = "QUERY")
    private String eventType;

    @Schema(description = "执行结果", example = "SUCCESS")
    private String result;

    @Schema(description = "操作人ID", example = "2001")
    private Long operatorId;

    @Schema(description = "操作人名称", example = "System Admin")
    private String operatorName;

    @Schema(description = "请求URI", example = "/upms/users/2001")
    private String requestUri;

    @Schema(description = "HTTP方法", example = "GET")
    private String httpMethod;

    @Schema(description = "耗时毫秒", example = "12")
    private Long costMs;

    @Schema(description = "发生时间")
    private Instant occurredAt;
}
