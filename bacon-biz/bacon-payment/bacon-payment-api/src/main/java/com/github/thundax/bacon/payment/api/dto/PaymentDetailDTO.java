package com.github.thundax.bacon.payment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 支付详情传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailDTO {

    /** 所属租户主键。 */
    private Long tenantId;
    /** 支付单号。 */
    private String paymentNo;
    /** 关联订单号。 */
    private String orderNo;
    /** 支付用户主键。 */
    private Long userId;
    /** 支付渠道编码。 */
    private String channelCode;
    /** 支付状态。 */
    private String paymentStatus;
    /** 支付金额。 */
    private BigDecimal amount;
    /** 已支付金额。 */
    private BigDecimal paidAmount;
    /** 创建时间。 */
    private Instant createdAt;
    /** 过期时间。 */
    private Instant expiredAt;
    /** 支付完成时间。 */
    private Instant paidAt;
    /** 支付标题。 */
    private String subject;
    /** 关闭时间。 */
    private Instant closedAt;
    /** 支付渠道交易号。 */
    private String channelTransactionNo;
    /** 支付渠道状态。 */
    private String channelStatus;
    /** 最近一次回调摘要。 */
    private String callbackSummary;
}
