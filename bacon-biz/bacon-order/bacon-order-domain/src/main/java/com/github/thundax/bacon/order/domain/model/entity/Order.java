package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.util.MoneyValidator;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.domain.exception.OrderDomainException;
import com.github.thundax.bacon.order.domain.exception.OrderErrorCode;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannel;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 订单主单领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order {

    /** 订单主键。 */
    private OrderId id;
    /** 订单号。 */
    private OrderNo orderNo;
    /** 下单用户主键。 */
    private UserId userId;
    /** 订单状态。 */
    private OrderStatus orderStatus;
    /** 支付状态。 */
    private PayStatus payStatus;
    /** 库存状态。 */
    private InventoryStatus inventoryStatus;
    /** 支付单号。 */
    private PaymentNo paymentNo;
    /** 库存预占单号。 */
    private ReservationNo reservationNo;
    /** 币种编码。 */
    private CurrencyCode currencyCode;
    /** 订单总金额。 */
    private Money totalAmount;
    /** 应付金额。 */
    private Money payableAmount;
    /** 订单备注。 */
    private String remark;
    /** 支付渠道编码。 */
    private PaymentChannel paymentChannelCode;
    /** 支付成功金额。 */
    private Money paidAmount;
    /** 支付渠道状态。 */
    private String paymentChannelStatus;
    /** 支付失败原因。 */
    private String paymentFailureReason;
    /** 支付失败时间。 */
    private Instant paymentFailedAt;
    /** 仓库业务编码。 */
    private WarehouseCode warehouseCode;
    /** 库存失败原因。 */
    private String inventoryFailureReason;
    /** 库存释放原因。 */
    private String inventoryReleaseReason;
    /** 取消原因。 */
    private String cancelReason;
    /** 关闭原因。 */
    private String closeReason;
    /** 创建时间。 */
    private Instant createdAt;
    /** 过期时间。 */
    private Instant expiredAt;
    /** 支付完成时间。 */
    private Instant paidAt;
    /** 库存释放时间。 */
    private Instant inventoryReleasedAt;
    /** 库存扣减时间。 */
    private Instant inventoryDeductedAt;
    /** 订单关闭时间。 */
    private Instant closedAt;

    public static Order create(
            OrderId id,
            OrderNo orderNo,
            UserId userId,
            CurrencyCode currencyCode,
            List<OrderItem> items,
            String remark,
            Instant expiredAt) {
        CurrencyCode resolvedCurrencyCode = resolveCurrencyCode(currencyCode);
        Money resolvedTotalAmount = sumLineAmounts(items, resolvedCurrencyCode);
        MoneyValidator.ensureSameCurrency(resolvedCurrencyCode, resolvedTotalAmount, resolvedTotalAmount, null);
        MoneyValidator.ensureSameCurrency(resolvedTotalAmount, resolvedTotalAmount, null);
        Instant createdAt = Instant.now();
        Instant resolvedExpiredAt = expiredAt == null ? createdAt.plusSeconds(1800) : expiredAt;
        return new Order(
                id,
                orderNo,
                userId,
                OrderStatus.CREATED,
                PayStatus.UNPAID,
                InventoryStatus.UNRESERVED,
                null,
                null,
                resolvedCurrencyCode,
                resolvedTotalAmount,
                resolvedTotalAmount,
                remark,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                createdAt,
                resolvedExpiredAt,
                null,
                null,
                null,
                null);
    }

    public static Order reconstruct(
            OrderId id,
            OrderNo orderNo,
            UserId userId,
            OrderStatus orderStatus,
            PayStatus payStatus,
            InventoryStatus inventoryStatus,
            PaymentNo paymentNo,
            ReservationNo reservationNo,
            CurrencyCode currencyCode,
            Money totalAmount,
            Money payableAmount,
            String remark,
            String cancelReason,
            String closeReason,
            Instant createdAt,
            Instant expiredAt,
            Instant paidAt,
            Instant closedAt,
            PaymentChannel paymentChannelCode,
            Money paidAmount,
            String paymentChannelStatus,
            String paymentFailureReason,
            Instant paymentFailedAt,
            WarehouseCode warehouseCode,
            String inventoryFailureReason,
            String inventoryReleaseReason,
            Instant inventoryReleasedAt,
            Instant inventoryDeductedAt) {
        Instant resolvedCreatedAt = createdAt == null ? Instant.now() : createdAt;
        Instant resolvedExpiredAt = expiredAt == null ? resolvedCreatedAt.plusSeconds(1800) : expiredAt;
        // 持久化重建必须保留主单与支付/库存派生状态，避免查询和回写时把终态信息重置成初始值。
        return new Order(
                id,
                orderNo,
                userId,
                orderStatus,
                payStatus,
                inventoryStatus,
                paymentNo,
                reservationNo,
                currencyCode,
                Money.resolve(totalAmount, currencyCode),
                Money.resolve(payableAmount, currencyCode),
                remark,
                paymentChannelCode,
                Money.resolve(paidAmount, currencyCode),
                paymentChannelStatus,
                paymentFailureReason,
                paymentFailedAt,
                warehouseCode,
                inventoryFailureReason,
                inventoryReleaseReason,
                cancelReason,
                closeReason,
                resolvedCreatedAt,
                resolvedExpiredAt,
                paidAt,
                inventoryReleasedAt,
                inventoryDeductedAt,
                closedAt);
    }

    public void markReservingStock() {
        // 只有新建订单才能进入预占库存阶段，避免取消/关闭后的订单再次触发库存链路。
        ensureOrderStatus(OrderStatus.CREATED);
        this.orderStatus = OrderStatus.RESERVING_STOCK;
        this.inventoryStatus = InventoryStatus.RESERVING;
    }

    public void markInventoryReserved(ReservationNo reservationNo, WarehouseCode warehouseCode) {
        ensureOrderStatus(OrderStatus.RESERVING_STOCK);
        this.reservationNo = reservationNo;
        this.warehouseCode = warehouseCode;
        this.inventoryStatus = InventoryStatus.RESERVED;
    }

    public boolean recordInventoryReservationResult(
            InventoryStatus inventoryStatus,
            ReservationNo reservationNo,
            WarehouseCode warehouseCode,
            String failureReason,
            String closeReason) {
        // 预占结果先收口为库存派生状态，再决定主单是继续创建支付还是立即关单。
        if (isInventoryReserved(inventoryStatus)) {
            markInventoryReserved(reservationNo, warehouseCode);
            return true;
        }
        markInventoryFailed(reservationNo, warehouseCode, failureReason);
        closeByInventoryReserveFailed(closeReason);
        return false;
    }

    public void markInventoryReleased(
            ReservationNo reservationNo, WarehouseCode warehouseCode, String releaseReason, Instant releasedAt) {
        this.reservationNo = reservationNo;
        this.warehouseCode = warehouseCode;
        this.inventoryReleaseReason = releaseReason;
        this.inventoryReleasedAt = releasedAt;
        this.inventoryStatus = InventoryStatus.RELEASED;
    }

    public void recordInventoryReleaseResult(
            InventoryStatus inventoryStatus,
            ReservationNo reservationNo,
            WarehouseCode warehouseCode,
            String releaseReason,
            Instant releasedAt,
            String failureReason) {
        // 释放结果只回写库存侧事实，不反向改变已经确定的取消/关闭主状态。
        if (isInventoryReleased(inventoryStatus)) {
            markInventoryReleased(reservationNo, warehouseCode, releaseReason, releasedAt);
        } else {
            markInventoryFailed(reservationNo, warehouseCode, failureReason);
        }
    }

    public void markInventoryDeducted(ReservationNo reservationNo, WarehouseCode warehouseCode, Instant deductedAt) {
        this.reservationNo = reservationNo;
        this.warehouseCode = warehouseCode;
        this.inventoryDeductedAt = deductedAt;
        this.inventoryStatus = InventoryStatus.DEDUCTED;
    }

    public boolean recordInventoryDeductionResult(
            InventoryStatus inventoryStatus,
            ReservationNo reservationNo,
            WarehouseCode warehouseCode,
            Instant deductedAt,
            String failureReason) {
        // 扣减结果发生在支付成功之后，这里只沉淀库存侧结果，由调用方决定是否重试或报错。
        if (isInventoryDeducted(inventoryStatus)) {
            markInventoryDeducted(reservationNo, warehouseCode, deductedAt);
            return true;
        }
        markInventoryFailed(reservationNo, warehouseCode, failureReason);
        return false;
    }

    public void markInventoryFailed(ReservationNo reservationNo, WarehouseCode warehouseCode, String failureReason) {
        this.reservationNo = reservationNo;
        this.warehouseCode = warehouseCode;
        this.inventoryFailureReason = failureReason;
        this.inventoryStatus = InventoryStatus.FAILED;
    }

    public void markPendingPayment(PaymentNo paymentNo, String channelCode) {
        ensureOrderStatus(OrderStatus.RESERVING_STOCK);
        this.orderStatus = OrderStatus.PENDING_PAYMENT;
        this.payStatus = PayStatus.PAYING;
        this.paymentNo = paymentNo;
        this.paymentChannelCode = PaymentChannel.from(channelCode);
    }

    public boolean recordPaymentCreationResult(
            PaymentNo paymentNo, PayStatus payStatus, String channelCode, String closeReason) {
        // 支付单创建成功才允许主单进入待支付；否则直接在创建支付阶段关单。
        if (canEnterPendingPayment(paymentNo, payStatus)) {
            markPendingPayment(paymentNo, channelCode);
            return true;
        }
        closeByPaymentCreateFailed(closeReason);
        return false;
    }

    public void markPaid(PaymentNo paymentNo, String channelCode, Money paidAmount, Instant paidTime) {
        ensureOrderStatus(OrderStatus.PENDING_PAYMENT);
        MoneyValidator.ensureSameCurrency(totalAmount, paidAmount);
        this.orderStatus = OrderStatus.PAID;
        this.payStatus = PayStatus.PAID;
        this.paymentNo = paymentNo;
        this.paymentChannelCode = PaymentChannel.from(channelCode);
        this.paidAmount = paidAmount;
        this.paidAt = paidTime;
    }

    public void markPaymentFailed(PaymentNo paymentNo, String reason, String channelStatus, Instant failedAt) {
        ensureOrderStatus(OrderStatus.PENDING_PAYMENT);
        this.orderStatus = OrderStatus.CLOSED;
        this.payStatus = PayStatus.FAILED;
        this.paymentNo = paymentNo;
        this.paymentFailureReason = reason;
        this.paymentChannelStatus = channelStatus;
        this.paymentFailedAt = failedAt;
        this.closeReason = reason;
        this.closedAt = Instant.now();
    }

    public void closeByInventoryReserveFailed(String reason) {
        ensureOrderStatus(OrderStatus.RESERVING_STOCK);
        this.orderStatus = OrderStatus.CLOSED;
        this.closeReason = reason;
        this.closedAt = Instant.now();
    }

    public void closeByPaymentCreateFailed(String reason) {
        ensureOrderStatus(OrderStatus.RESERVING_STOCK);
        this.orderStatus = OrderStatus.CLOSED;
        this.closeReason = reason;
        this.closedAt = Instant.now();
    }

    public void closeExpired(String reason) {
        ensureOrderStatus(OrderStatus.CREATED, OrderStatus.PENDING_PAYMENT);
        this.orderStatus = OrderStatus.CLOSED;
        this.payStatus = PayStatus.CLOSED;
        this.closeReason = reason;
        this.closedAt = Instant.now();
    }

    public void cancel(String reason) {
        ensureOrderStatus(OrderStatus.CREATED, OrderStatus.PENDING_PAYMENT, OrderStatus.RESERVING_STOCK);
        this.orderStatus = OrderStatus.CANCELLED;
        this.payStatus = PayStatus.CLOSED;
        this.cancelReason = reason;
        this.closedAt = Instant.now();
    }

    private void ensureOrderStatus(OrderStatus... expectedStatuses) {
        for (OrderStatus expectedStatus : expectedStatuses) {
            if (expectedStatus == orderStatus) {
                return;
            }
        }
        throw new OrderDomainException(
                OrderErrorCode.INVALID_ORDER_STATUS,
                orderStatus == null ? null : orderStatus.value());
    }

    private boolean isInventoryReserved(InventoryStatus inventoryStatus) {
        return InventoryStatus.RESERVED == inventoryStatus;
    }

    private boolean isInventoryReleased(InventoryStatus inventoryStatus) {
        return InventoryStatus.RELEASED == inventoryStatus;
    }

    private boolean isInventoryDeducted(InventoryStatus inventoryStatus) {
        return InventoryStatus.DEDUCTED == inventoryStatus;
    }

    private boolean canEnterPendingPayment(PaymentNo paymentNo, PayStatus payStatus) {
        return paymentNo != null && PayStatus.PAYING == payStatus;
    }

    private static CurrencyCode resolveCurrencyCode(CurrencyCode currencyCode) {
        return currencyCode == null ? CurrencyCode.RMB : currencyCode;
    }

    private static Money sumLineAmounts(List<OrderItem> items, CurrencyCode currencyCode) {
        Money totalAmount = Money.of(BigDecimal.ZERO, currencyCode);
        if (items == null || items.isEmpty()) {
            return totalAmount;
        }
        for (OrderItem item : items) {
            MoneyValidator.ensureSameCurrency(totalAmount, item.getLineAmount());
            totalAmount = Money.of(totalAmount.value().add(item.getLineAmount().value()), currencyCode);
        }
        return totalAmount;
    }
}
