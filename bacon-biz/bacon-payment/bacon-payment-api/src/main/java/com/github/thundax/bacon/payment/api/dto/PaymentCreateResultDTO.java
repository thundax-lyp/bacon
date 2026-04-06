package com.github.thundax.bacon.payment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 支付创建结果传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateResultDTO {

    /** 所属租户主键。 */
    private Long tenantId;
    /** 支付单号。 */
    private String paymentNo;
    /** 关联订单号。 */
    private String orderNo;
    /** 支付渠道编码。 */
    private String channelCode;
    /** 支付状态。 */
    private String paymentStatus;
    /** 拉起支付载荷。 */
    private String payPayload;
    /** 过期时间。 */
    private Instant expiredAt;
    /** 失败原因。 */
    private String failureReason;
}
