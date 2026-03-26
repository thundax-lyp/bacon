package com.github.thundax.bacon.order.domain.model.entity;

import java.math.BigDecimal;
import java.time.Instant;

public class Order {

    private Long id;
    private final Long tenantId;
    private final String orderNo;
    private final Long userId;
    private String orderStatus;
    private String payStatus;
    private String inventoryStatus;
    private String paymentNo;
    private String reservationNo;
    private String currencyCode;
    private BigDecimal totalAmount;
    private BigDecimal payableAmount;
    private final String remark;
    private String cancelReason;
    private String closeReason;
    private final Instant createdAt;
    private final Instant expiredAt;
    private Instant paidAt;
    private Instant closedAt;

    public Order(Long id, Long tenantId, String orderNo, Long userId, String currencyCode,
                 BigDecimal totalAmount, BigDecimal payableAmount, String remark, Instant expiredAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.orderNo = orderNo;
        this.userId = userId;
        this.orderStatus = "CREATED";
        this.payStatus = "UNPAID";
        this.inventoryStatus = "UNRESERVED";
        this.currencyCode = currencyCode;
        this.totalAmount = totalAmount;
        this.payableAmount = payableAmount;
        this.remark = remark;
        this.createdAt = Instant.now();
        this.expiredAt = expiredAt == null ? this.createdAt.plusSeconds(1800) : expiredAt;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRemark() {
        return remark;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getPayStatus() {
        return payStatus;
    }

    public String getInventoryStatus() {
        return inventoryStatus;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public String getReservationNo() {
        return reservationNo;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getPayableAmount() {
        return payableAmount;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiredAt() {
        return expiredAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void markPaid(String paymentNo, Instant paidTime) {
        this.orderStatus = "PAID";
        this.payStatus = "PAID";
        this.inventoryStatus = "DEDUCTED";
        this.paymentNo = paymentNo;
        this.paidAt = paidTime;
    }

    public void markPaymentFailed(String paymentNo, String reason) {
        this.orderStatus = "CLOSED";
        this.payStatus = "FAILED";
        this.inventoryStatus = "RELEASED";
        this.paymentNo = paymentNo;
        this.closeReason = reason;
        this.closedAt = Instant.now();
    }

    public void closeExpired(String reason) {
        this.orderStatus = "CLOSED";
        this.payStatus = "CLOSED";
        this.inventoryStatus = "RELEASED";
        this.closeReason = reason;
        this.closedAt = Instant.now();
    }

    public void cancel(String reason) {
        this.orderStatus = "CANCELLED";
        this.payStatus = "CLOSED";
        this.cancelReason = reason;
        this.inventoryStatus = "RELEASED";
        this.closedAt = Instant.now();
    }
}
