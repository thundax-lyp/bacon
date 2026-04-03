package com.github.thundax.bacon.payment.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_payment_order")
public class PaymentOrderDO {

    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("tenant_id")
    private String tenantId;
    @TableField("payment_no")
    private String paymentNo;
    @TableField("order_no")
    private String orderNo;
    @TableField("user_id")
    private String userId;
    @TableField("channel_code")
    private String channelCode;
    @TableField("payment_status")
    private String paymentStatus;
    @TableField("amount")
    private BigDecimal amount;
    @TableField("paid_amount")
    private BigDecimal paidAmount;
    @TableField("subject")
    private String subject;
    @TableField("created_at")
    private Instant createdAt;
    @TableField("updated_at")
    private Instant updatedAt;
    @TableField("expired_at")
    private Instant expiredAt;
    @TableField("paid_at")
    private Instant paidAt;
    @TableField("closed_at")
    private Instant closedAt;
}
