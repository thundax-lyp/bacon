package com.github.thundax.bacon.payment.infra.persistence.dataobject;

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
@TableName("bacon_payment_audit_log")
public class PaymentAuditLogDO {

    @TableId(type = IdType.INPUT)
    private Long id;
    @TableField("tenant_id")
    private String tenantId;
    @TableField("payment_no")
    private String paymentNo;
    @TableField("action_type")
    private String actionType;
    @TableField("before_status")
    private String beforeStatus;
    @TableField("after_status")
    private String afterStatus;
    @TableField("operator_type")
    private String operatorType;
    @TableField("operator_id")
    private String operatorId;
    @TableField("occurred_at")
    private Instant occurredAt;
}
