package com.github.thundax.bacon.order.api.response;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 订单摘要门面响应。
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryFacadeResponse {

    private String orderNo;
    private Long userId;
    private String orderStatus;
    private String payStatus;
    private String inventoryStatus;
    private String paymentNo;
    private String reservationNo;
    private String currencyCode;
    private BigDecimal totalAmount;
    private BigDecimal payableAmount;
    private String cancelReason;
    private String closeReason;
    private Instant createdAt;
    private Instant expiredAt;
}
