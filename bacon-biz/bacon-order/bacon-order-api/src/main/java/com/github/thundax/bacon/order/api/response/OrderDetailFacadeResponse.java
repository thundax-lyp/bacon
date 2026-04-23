package com.github.thundax.bacon.order.api.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 订单详情门面响应。
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailFacadeResponse {

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
    private List<OrderItemFacadeResponse> items;
    private String paymentSnapshot;
    private String inventorySnapshot;
    private Instant paidAt;
    private Instant closedAt;
}
