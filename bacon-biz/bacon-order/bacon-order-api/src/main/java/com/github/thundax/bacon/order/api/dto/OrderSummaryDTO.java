package com.github.thundax.bacon.order.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 订单摘要传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDTO {

    /** 订单主键。 */
    private String id;
    /** 所属租户主键。 */
    private String tenantId;
    /** 订单号。 */
    private String orderNo;
    /** 下单用户主键。 */
    private String userId;
    /** 订单状态。 */
    private String orderStatus;
    /** 支付状态。 */
    private String payStatus;
    /** 库存状态。 */
    private String inventoryStatus;
    /** 支付单号。 */
    private String paymentNo;
    /** 库存预占单号。 */
    private String reservationNo;
    /** 币种编码。 */
    private String currencyCode;
    /** 订单总金额。 */
    private BigDecimal totalAmount;
    /** 应付金额。 */
    private BigDecimal payableAmount;
    /** 取消原因。 */
    private String cancelReason;
    /** 关闭原因。 */
    private String closeReason;
    /** 创建时间。 */
    private Instant createdAt;
    /** 过期时间。 */
    private Instant expiredAt;
}
