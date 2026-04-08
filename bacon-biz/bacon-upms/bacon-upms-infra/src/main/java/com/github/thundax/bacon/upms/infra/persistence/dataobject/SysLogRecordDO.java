package com.github.thundax.bacon.upms.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.id.domain.OperatorId;
import java.time.Instant;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_upms_sys_log")
public class SysLogRecordDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("tenant_id")
    private Long tenantId;
    @TableField("trace_id")
    private String traceId;
    @TableField("request_id")
    private String requestId;
    @TableField("module")
    private String module;
    @TableField("action")
    private String action;
    @TableField("event_type")
    private String eventType;
    @TableField("result")
    private String result;
    @TableField("operator_id")
    private OperatorId operatorId;
    @TableField("operator_name")
    private String operatorName;
    @TableField("client_ip")
    private String clientIp;
    @TableField("request_uri")
    private String requestUri;
    @TableField("http_method")
    private String httpMethod;
    @TableField("cost_ms")
    private Long costMs;
    @TableField("error_message")
    private String errorMessage;
    @TableField("occurred_at")
    private Instant occurredAt;
    @TableField("created_by")
    private String createdBy;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_by")
    private String updatedBy;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
