package com.github.thundax.bacon.order.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_order_payment_snapshot")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class OrderPaymentSnapshotDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("order_id")
    private Long orderId;

    @TableField("payment_no")
    private String paymentNo;

    @TableField("channel_code")
    private String channelCode;

    @TableField("pay_status")
    private String payStatus;

    @TableField("currency_code")
    private String currencyCode;

    @TableField("paid_amount")
    private BigDecimal paidAmount;

    @TableField("paid_time")
    private Instant paidTime;

    @TableField("failure_reason")
    private String failureReason;

    @TableField("channel_status")
    private String channelStatus;

    @TableField("updated_at")
    private Instant updatedAt;
}
