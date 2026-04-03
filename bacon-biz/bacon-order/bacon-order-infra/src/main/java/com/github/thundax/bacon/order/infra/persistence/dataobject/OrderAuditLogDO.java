package com.github.thundax.bacon.order.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_order_audit_log")
public class OrderAuditLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("tenant_id")
    private Long tenantId;
    @TableField("order_no")
    private String orderNo;
    @TableField("action_type")
    private String actionType;
    @TableField("before_status")
    private String beforeStatus;
    @TableField("after_status")
    private String afterStatus;
    @TableField("operator_type")
    private String operatorType;
    @TableField("operator_id")
    private Long operatorId;
    @TableField("occurred_at")
    private Instant occurredAt;
}
