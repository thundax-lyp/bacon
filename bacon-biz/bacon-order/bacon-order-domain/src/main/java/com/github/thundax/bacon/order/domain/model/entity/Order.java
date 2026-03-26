package com.github.thundax.bacon.order.domain.model.entity;

import java.math.BigDecimal;
import java.time.Instant;

public class Order {

    public static final String ORDER_STATUS_CREATED = "CREATED";
    public static final String ORDER_STATUS_RESERVING_STOCK = "RESERVING_STOCK";
    public static final String ORDER_STATUS_PENDING_PAYMENT = "PENDING_PAYMENT";
    public static final String ORDER_STATUS_PAID = "PAID";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    public static final String ORDER_STATUS_CLOSED = "CLOSED";

    public static final String PAY_STATUS_UNPAID = "UNPAID";
    public static final String PAY_STATUS_PAYING = "PAYING";
    public static final String PAY_STATUS_PAID = "PAID";
    public static final String PAY_STATUS_FAILED = "FAILED";
    public static final String PAY_STATUS_CLOSED = "CLOSED";

    public static final String INVENTORY_STATUS_UNRESERVED = "UNRESERVED";
    public static final String INVENTORY_STATUS_RESERVING = "RESERVING";
    public static final String INVENTORY_STATUS_RESERVED = "RESERVED";
    public static final String INVENTORY_STATUS_RELEASED = "RELEASED";
    public static final String INVENTORY_STATUS_DEDUCTED = "DEDUCTED";
    public static final String INVENTORY_STATUS_FAILED = "FAILED";

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
    private String paymentChannelCode;
    private BigDecimal paidAmount;
    private String paymentChannelStatus;
    private String paymentFailureReason;
    private Instant paymentFailedAt;
    private Long warehouseId;
    private String inventoryFailureReason;
    private String inventoryReleaseReason;
    private Instant inventoryReleasedAt;
    private Instant inventoryDeductedAt;

    public Order(Long id, Long tenantId, String orderNo, Long userId, String currencyCode,
                 BigDecimal totalAmount, BigDecimal payableAmount, String remark, Instant expiredAt) {
        this(id, tenantId, orderNo, userId, ORDER_STATUS_CREATED, PAY_STATUS_UNPAID, INVENTORY_STATUS_UNRESERVED,
                null, null, currencyCode, totalAmount, payableAmount, remark, null, null, Instant.now(),
                expiredAt, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private Order(Long id, Long tenantId, String orderNo, Long userId, String orderStatus, String payStatus,
                  String inventoryStatus, String paymentNo, String reservationNo, String currencyCode,
                  BigDecimal totalAmount, BigDecimal payableAmount, String remark, String cancelReason,
                  String closeReason, Instant createdAt, Instant expiredAt, Instant paidAt, Instant closedAt,
                  String paymentChannelCode, BigDecimal paidAmount, String paymentChannelStatus,
                  String paymentFailureReason, Instant paymentFailedAt, Long warehouseId,
                  String inventoryFailureReason, String inventoryReleaseReason, Instant inventoryReleasedAt,
                  Instant inventoryDeductedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.orderNo = orderNo;
        this.userId = userId;
        this.orderStatus = orderStatus;
        this.payStatus = payStatus;
        this.inventoryStatus = inventoryStatus;
        this.paymentNo = paymentNo;
        this.reservationNo = reservationNo;
        this.currencyCode = currencyCode;
        this.totalAmount = totalAmount;
        this.payableAmount = payableAmount;
        this.remark = remark;
        this.cancelReason = cancelReason;
        this.closeReason = closeReason;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.expiredAt = expiredAt == null ? this.createdAt.plusSeconds(1800) : expiredAt;
        this.paidAt = paidAt;
        this.closedAt = closedAt;
        this.paymentChannelCode = paymentChannelCode;
        this.paidAmount = paidAmount;
        this.paymentChannelStatus = paymentChannelStatus;
        this.paymentFailureReason = paymentFailureReason;
        this.paymentFailedAt = paymentFailedAt;
        this.warehouseId = warehouseId;
        this.inventoryFailureReason = inventoryFailureReason;
        this.inventoryReleaseReason = inventoryReleaseReason;
        this.inventoryReleasedAt = inventoryReleasedAt;
        this.inventoryDeductedAt = inventoryDeductedAt;
    }

    public static Order rehydrate(Long id, Long tenantId, String orderNo, Long userId, String orderStatus, String payStatus,
                                  String inventoryStatus, String paymentNo, String reservationNo, String currencyCode,
                                  BigDecimal totalAmount, BigDecimal payableAmount, String remark, String cancelReason,
                                  String closeReason, Instant createdAt, Instant expiredAt, Instant paidAt,
                                  Instant closedAt, String paymentChannelCode, BigDecimal paidAmount,
                                  String paymentChannelStatus, String paymentFailureReason, Instant paymentFailedAt,
                                  Long warehouseId, String inventoryFailureReason, String inventoryReleaseReason,
                                  Instant inventoryReleasedAt, Instant inventoryDeductedAt) {
        return new Order(id, tenantId, orderNo, userId, orderStatus, payStatus, inventoryStatus, paymentNo,
                reservationNo, currencyCode, totalAmount, payableAmount, remark, cancelReason, closeReason, createdAt,
                expiredAt, paidAt, closedAt, paymentChannelCode, paidAmount, paymentChannelStatus, paymentFailureReason,
                paymentFailedAt, warehouseId, inventoryFailureReason, inventoryReleaseReason, inventoryReleasedAt,
                inventoryDeductedAt);
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

    public String getPaymentChannelCode() {
        return paymentChannelCode;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public String getPaymentChannelStatus() {
        return paymentChannelStatus;
    }

    public String getPaymentFailureReason() {
        return paymentFailureReason;
    }

    public Instant getPaymentFailedAt() {
        return paymentFailedAt;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public String getInventoryFailureReason() {
        return inventoryFailureReason;
    }

    public String getInventoryReleaseReason() {
        return inventoryReleaseReason;
    }

    public Instant getInventoryReleasedAt() {
        return inventoryReleasedAt;
    }

    public Instant getInventoryDeductedAt() {
        return inventoryDeductedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void markReservingStock() {
        ensureOrderStatus(ORDER_STATUS_CREATED);
        this.orderStatus = ORDER_STATUS_RESERVING_STOCK;
        this.inventoryStatus = INVENTORY_STATUS_RESERVING;
    }

    public void markInventoryReserved(String reservationNo, Long warehouseId) {
        ensureOrderStatus(ORDER_STATUS_RESERVING_STOCK);
        this.reservationNo = reservationNo;
        this.warehouseId = warehouseId;
        this.inventoryStatus = INVENTORY_STATUS_RESERVED;
    }

    public void markInventoryReleased(String reservationNo, Long warehouseId, String releaseReason, Instant releasedAt) {
        this.reservationNo = reservationNo;
        this.warehouseId = warehouseId;
        this.inventoryReleaseReason = releaseReason;
        this.inventoryReleasedAt = releasedAt;
        this.inventoryStatus = INVENTORY_STATUS_RELEASED;
    }

    public void markInventoryDeducted(String reservationNo, Long warehouseId, Instant deductedAt) {
        this.reservationNo = reservationNo;
        this.warehouseId = warehouseId;
        this.inventoryDeductedAt = deductedAt;
        this.inventoryStatus = INVENTORY_STATUS_DEDUCTED;
    }

    public void markInventoryFailed(String reservationNo, Long warehouseId, String failureReason) {
        this.reservationNo = reservationNo;
        this.warehouseId = warehouseId;
        this.inventoryFailureReason = failureReason;
        this.inventoryStatus = INVENTORY_STATUS_FAILED;
    }

    public void markPendingPayment(String paymentNo, String channelCode) {
        ensureOrderStatus(ORDER_STATUS_RESERVING_STOCK);
        this.orderStatus = ORDER_STATUS_PENDING_PAYMENT;
        this.payStatus = PAY_STATUS_PAYING;
        this.paymentNo = paymentNo;
        this.paymentChannelCode = channelCode;
    }

    public void markPaid(String paymentNo, String channelCode, BigDecimal paidAmount, Instant paidTime) {
        ensureOrderStatus(ORDER_STATUS_PENDING_PAYMENT);
        this.orderStatus = ORDER_STATUS_PAID;
        this.payStatus = PAY_STATUS_PAID;
        this.paymentNo = paymentNo;
        this.paymentChannelCode = channelCode;
        this.paidAmount = paidAmount;
        this.paidAt = paidTime;
    }

    public void markPaymentFailed(String paymentNo, String reason, String channelStatus, Instant failedAt) {
        ensureOrderStatus(ORDER_STATUS_PENDING_PAYMENT);
        this.orderStatus = ORDER_STATUS_CLOSED;
        this.payStatus = PAY_STATUS_FAILED;
        this.paymentNo = paymentNo;
        this.paymentFailureReason = reason;
        this.paymentChannelStatus = channelStatus;
        this.paymentFailedAt = failedAt;
        this.closeReason = reason;
        this.closedAt = Instant.now();
    }

    public void closeByInventoryReserveFailed(String reason) {
        ensureOrderStatus(ORDER_STATUS_RESERVING_STOCK);
        this.orderStatus = ORDER_STATUS_CLOSED;
        this.closeReason = reason;
        this.closedAt = Instant.now();
    }

    public void closeByPaymentCreateFailed(String reason) {
        ensureOrderStatus(ORDER_STATUS_RESERVING_STOCK);
        this.orderStatus = ORDER_STATUS_CLOSED;
        this.closeReason = reason;
        this.closedAt = Instant.now();
    }

    public void closeExpired(String reason) {
        ensureOrderStatus(ORDER_STATUS_CREATED, ORDER_STATUS_PENDING_PAYMENT);
        this.orderStatus = ORDER_STATUS_CLOSED;
        this.payStatus = PAY_STATUS_CLOSED;
        this.closeReason = reason;
        this.closedAt = Instant.now();
    }

    public void cancel(String reason) {
        ensureOrderStatus(ORDER_STATUS_CREATED, ORDER_STATUS_PENDING_PAYMENT, ORDER_STATUS_RESERVING_STOCK);
        this.orderStatus = ORDER_STATUS_CANCELLED;
        this.payStatus = PAY_STATUS_CLOSED;
        this.cancelReason = reason;
        this.closedAt = Instant.now();
    }

    private void ensureOrderStatus(String... expectedStatuses) {
        for (String expectedStatus : expectedStatuses) {
            if (expectedStatus.equals(orderStatus)) {
                return;
            }
        }
        throw new IllegalStateException("Invalid order status: " + orderStatus);
    }
}
