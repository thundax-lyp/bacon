package com.github.thundax.bacon.payment.interfaces.response;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 支付详情响应对象。
 */
public record PaymentDetailResponse(
        /** 所属租户主键。 */
        String tenantId,
        /** 支付单号。 */
        String paymentNo,
        /** 关联订单号。 */
        String orderNo,
        /** 支付用户主键。 */
        String userId,
        /** 支付渠道编码。 */
        String channelCode,
        /** 支付状态。 */
        String paymentStatus,
        /** 支付金额。 */
        BigDecimal amount,
        /** 已支付金额。 */
        BigDecimal paidAmount,
        /** 创建时间。 */
        Instant createdAt,
        /** 过期时间。 */
        Instant expiredAt,
        /** 支付完成时间。 */
        Instant paidAt,
        /** 支付标题。 */
        String subject,
        /** 关闭时间。 */
        Instant closedAt,
        /** 支付渠道交易号。 */
        String channelTransactionNo,
        /** 支付渠道状态。 */
        String channelStatus,
        /** 最近一次回调摘要。 */
        String callbackSummary) {
}
