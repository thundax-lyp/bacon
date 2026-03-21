package com.github.thundax.bacon.order.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryDTO {

    private Long id;
    private Long tenantId;
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
