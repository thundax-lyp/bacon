package com.github.thundax.bacon.payment.domain.model.entity;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
public class PaymentOrder {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_PAYING = "PAYING";
    public static final String STATUS_PAID = "PAID";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_CLOSED = "CLOSED";
    public static final String CHANNEL_MOCK = "MOCK";

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
        this.paymentStatus = STATUS_CREATED;
        this.paidAmount = BigDecimal.ZERO;
    }

    public static PaymentOrder rehydrate(Long id, Long tenantId, String paymentNo, String orderNo, Long userId,
                                         String channelCode, BigDecimal amount, BigDecimal paidAmount, String subject,
                                         Instant createdAt, Instant expiredAt, Instant paidAt, Instant closedAt,
                                         String paymentStatus, String channelTransactionNo, String channelStatus,
                                         String callbackSummary) {
        PaymentOrder paymentOrder = new PaymentOrder(id, tenantId, paymentNo, orderNo, userId, channelCode,
                amount, subject, expiredAt, createdAt);
        paymentOrder.paidAmount = paidAmount == null ? BigDecimal.ZERO : paidAmount;
        paymentOrder.paidAt = paidAt;
        paymentOrder.closedAt = closedAt;
        paymentOrder.paymentStatus = paymentStatus;
        paymentOrder.channelTransactionNo = channelTransactionNo;
        paymentOrder.channelStatus = channelStatus;
        paymentOrder.callbackSummary = callbackSummary;
        return paymentOrder;
    }

    public void markPaying() {
        if (STATUS_PAID.equals(paymentStatus) || STATUS_FAILED.equals(paymentStatus) || STATUS_CLOSED.equals(paymentStatus)) {
            return;
        }
        this.paymentStatus = STATUS_PAYING;
    }

    public void markPaid(BigDecimal paidAmount, Instant paidTime, String channelTransactionNo, String channelStatus,
                         String callbackSummary) {
        if (STATUS_PAID.equals(paymentStatus) || STATUS_FAILED.equals(paymentStatus) || STATUS_CLOSED.equals(paymentStatus)) {
            return;
        }
        this.paymentStatus = STATUS_PAID;
        this.paidAmount = paidAmount;
        this.paidAt = paidTime;
        this.channelTransactionNo = channelTransactionNo;
        this.channelStatus = channelStatus;
        this.callbackSummary = callbackSummary;
    }

    public void markFailed(String channelStatus, String callbackSummary) {
        if (STATUS_PAID.equals(paymentStatus) || STATUS_FAILED.equals(paymentStatus) || STATUS_CLOSED.equals(paymentStatus)) {
            return;
        }
        this.paymentStatus = STATUS_FAILED;
        this.channelStatus = channelStatus;
        this.callbackSummary = callbackSummary;
    }

    public void close(Instant closeTime) {
        if (STATUS_PAID.equals(paymentStatus) || STATUS_FAILED.equals(paymentStatus) || STATUS_CLOSED.equals(paymentStatus)) {
            return;
        }
        this.paymentStatus = STATUS_CLOSED;
        this.closedAt = closeTime;
    }
}
