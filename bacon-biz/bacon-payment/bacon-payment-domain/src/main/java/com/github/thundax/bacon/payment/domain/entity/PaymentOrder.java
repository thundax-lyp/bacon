package com.github.thundax.bacon.payment.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;

@Getter
public class PaymentOrder {

    private final Long id;
    private final Long tenantId;
    private final String paymentNo;
    private final String orderNo;
    private final Long userId;
    private final String channelCode;
    private final BigDecimal amount;
    private final String subject;
    private final Instant expiredAt;
    private final Instant createdAt;
    private String paymentStatus;
    private BigDecimal paidAmount;
    private Instant paidAt;
    private Instant closedAt;
    private String channelTransactionNo;
    private String channelStatus;
    private String callbackSummary;

    public PaymentOrder(Long id, Long tenantId, String paymentNo, String orderNo, Long userId, String channelCode,
                        BigDecimal amount, String subject, Instant expiredAt, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.paymentNo = paymentNo;
        this.orderNo = orderNo;
        this.userId = userId;
        this.channelCode = channelCode;
        this.amount = amount;
        this.subject = subject;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
        this.paymentStatus = "PAYING";
        this.paidAmount = BigDecimal.ZERO;
    }

    public void markPaid(BigDecimal paidAmount, Instant paidTime, String channelTransactionNo, String channelStatus,
                         String callbackSummary) {
        this.paymentStatus = "PAID";
        this.paidAmount = paidAmount;
        this.paidAt = paidTime;
        this.channelTransactionNo = channelTransactionNo;
        this.channelStatus = channelStatus;
        this.callbackSummary = callbackSummary;
    }

    public void markFailed(String channelStatus, String callbackSummary) {
        this.paymentStatus = "FAILED";
        this.channelStatus = channelStatus;
        this.callbackSummary = callbackSummary;
    }

    public void close(Instant closeTime) {
        this.paymentStatus = "CLOSED";
        this.closedAt = closeTime;
    }
}
