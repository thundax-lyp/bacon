package com.github.thundax.bacon.payment.api.response;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailFacadeResponse {

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
    private String subject;
    private Instant closedAt;
    private String channelTransactionNo;
    private String channelStatus;
    private String callbackSummary;
}
