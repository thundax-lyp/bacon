package com.github.thundax.bacon.order.infra.persistence.dataobject;

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
@TableName("bacon_order_order")
public class OrderDO {

    @TableId(type = IdType.INPUT)
    private String id;
    @TableField("tenant_id")
    private String tenantId;
    @TableField("order_no")
    private String orderNo;
    @TableField("user_id")
    private String userId;
    @TableField("order_status")
    private String orderStatus;
    @TableField("pay_status")
    private String payStatus;
    @TableField("inventory_status")
    private String inventoryStatus;
    @TableField("currency_code")
    private String currencyCode;
    @TableField("total_amount")
    private BigDecimal totalAmount;
    @TableField("payable_amount")
    private BigDecimal payableAmount;
    @TableField("remark")
    private String remark;
    @TableField("cancel_reason")
    private String cancelReason;
    @TableField("close_reason")
    private String closeReason;
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
