package com.github.thundax.bacon.payment.infra.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.thundax.bacon.common.mybatis.annotation.TenantScoped;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("bacon_payment_callback_record")
@TenantScoped(read = true, insert = true, verifyOnUpdate = true)
public class PaymentCallbackRecordDO {

    @TableId(type = IdType.INPUT)
    private Long id;

    @TableField("tenant_id")
    private Long tenantId;

    @TableField("payment_no")
    private String paymentNo;

    @TableField("order_no")
    private String orderNo;

    @TableField("channel_code")
    private String channelCode;

    @TableField("channel_transaction_no")
    private String channelTransactionNo;

    @TableField("channel_status")
    private String channelStatus;

    @TableField("raw_payload")
    private String rawPayload;

    @TableField("received_at")
    private Instant receivedAt;
}
