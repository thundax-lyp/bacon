package com.github.thundax.bacon.order.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO {

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
    private List<OrderItemDTO> items;
    private String paymentSnapshot;
    private String inventorySnapshot;
    private Instant paidAt;
    private Instant closedAt;
}
