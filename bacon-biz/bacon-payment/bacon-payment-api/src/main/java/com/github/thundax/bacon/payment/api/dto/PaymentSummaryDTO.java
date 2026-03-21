package com.github.thundax.bacon.payment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryDTO {

    private Long tenantId;
    private String paymentNo;
    private String orderNo;
    private Long userId;
    private String channelCode;
    private String paymentStatus;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private Instant createdAt;
    private Instant expiredAt;
    private Instant paidAt;
}
