package com.github.thundax.bacon.order.domain.model.entity;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 订单支付快照。
 */
public record OrderPaymentSnapshot(
        /** 快照主键。 */
        Long id,
        /** 所属租户主键。 */
        Long tenantId,
        /** 订单主键。 */
        Long orderId,
        /** 支付单号。 */
        String paymentNo,
        /** 支付渠道编码。 */
        String channelCode,
        /** 支付状态。 */
        String payStatus,
        /** 已支付金额。 */
        BigDecimal paidAmount,
        /** 支付完成时间。 */
        Instant paidTime,
        /** 失败原因。 */
        String failureReason,
        /** 支付渠道状态。 */
        String channelStatus,
        /** 最后更新时间。 */
        Instant updatedAt
) {
}
