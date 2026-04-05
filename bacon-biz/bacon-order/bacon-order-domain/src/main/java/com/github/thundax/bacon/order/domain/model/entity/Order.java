package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.core.enums.CurrencyCode;
import com.github.thundax.bacon.common.core.util.MoneyValidator;
import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannel;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.model.valueobject.PaymentNo;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.order.domain.model.valueobject.WarehouseNo;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

/**
 * 订单主单领域实体。
 */
@Getter
public class Order {

    /** 订单主键。 */
    @Setter
    private OrderId id;
    /** 所属租户主键。 */
    private final TenantId tenantId;
    /** 订单号。 */
    private final OrderNo orderNo;
    /** 下单用户主键。 */
    private final UserId userId;
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
    private final CurrencyCode currencyCode;
    /** 订单总金额。 */
    private Money totalAmount;
    /** 应付金额。 */
    private Money payableAmount;
    /** 订单备注。 */
    private final String remark;
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
    /** 仓库业务编号。 */
    private WarehouseNo warehouseNo;
    /** 库存失败原因。 */
    private String inventoryFailureReason;
    /** 库存释放原因。 */
    private String inventoryReleaseReason;
    /** 取消原因。 */
    private String cancelReason;
    /** 关闭原因。 */
    private String closeReason;
    /** 创建时间。 */
    private final Instant createdAt;
    /** 过期时间。 */
    private final Instant expiredAt;
    /** 支付完成时间。 */
    private Instant paidAt;
    /** 库存释放时间。 */
    private Instant inventoryReleasedAt;
    /** 库存扣减时间。 */
    private Instant inventoryDeductedAt;
    /** 订单关闭时间。 */
    private Instant closedAt;

    public Order(OrderId id, TenantId tenantId, OrderNo orderNo, UserId userId, CurrencyCode currencyCode, Money totalAmount,
                 Money payableAmount, String remark, Instant expiredAt) {
        this(id, tenantId, orderNo, userId, OrderStatus.CREATED, PayStatus.UNPAID, InventoryStatus.UNRESERVED,
                null, null, currencyCode, totalAmount, payableAmount, remark, null, null, Instant.now(),
                expiredAt, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private Order(OrderId id, TenantId tenantId, OrderNo orderNo, UserId userId, OrderStatus orderStatus, PayStatus payStatus,
                  InventoryStatus inventoryStatus, PaymentNo paymentNo, ReservationNo reservationNo, CurrencyCode currencyCode, Money totalAmount,
                  Money payableAmount, String remark, String cancelReason,
                  String closeReason, Instant createdAt, Instant expiredAt, Instant paidAt, Instant closedAt,
                  PaymentChannel paymentChannelCode, Money paidAmount, String paymentChannelStatus,
                  String paymentFailureReason, Instant paymentFailedAt, WarehouseNo warehouseNo,
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
        MoneyValidator.ensureSameCurrency(currencyCode, totalAmount, payableAmount, paidAmount);
        this.currencyCode = currencyCode;
        MoneyValidator.ensureSameCurrency(totalAmount, payableAmount, paidAmount);
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
        this.warehouseNo = warehouseNo;
        this.inventoryFailureReason = inventoryFailureReason;
        this.inventoryReleaseReason = inventoryReleaseReason;
        this.inventoryReleasedAt = inventoryReleasedAt;
        this.inventoryDeductedAt = inventoryDeductedAt;
    }

    public static Order rehydrate(OrderId id, TenantId tenantId, OrderNo orderNo, UserId userId, OrderStatus orderStatus,
                                  PayStatus payStatus, InventoryStatus inventoryStatus, PaymentNo paymentNo, ReservationNo reservationNo,
                                  CurrencyCode currencyCode, Money totalAmount,
                                  Money payableAmount, String remark, String cancelReason,
                                  String closeReason, Instant createdAt, Instant expiredAt, Instant paidAt,
                                  Instant closedAt, PaymentChannel paymentChannelCode, Money paidAmount,
                                  String paymentChannelStatus, String paymentFailureReason, Instant paymentFailedAt,
                                  WarehouseNo warehouseNo, String inventoryFailureReason, String inventoryReleaseReason,
                                  Instant inventoryReleasedAt, Instant inventoryDeductedAt) {
        // 持久化重建必须保留主单与支付/库存派生状态，避免查询和回写时把终态信息重置成初始值。
        return new Order(id, tenantId, orderNo, userId, orderStatus, payStatus, inventoryStatus, paymentNo,
                reservationNo, currencyCode, totalAmount, payableAmount, remark, cancelReason, closeReason, createdAt,
                expiredAt, paidAt, closedAt, paymentChannelCode, paidAmount, paymentChannelStatus, paymentFailureReason,
                paymentFailedAt, warehouseNo, inventoryFailureReason, inventoryReleaseReason, inventoryReleasedAt,
                inventoryDeductedAt);
    }

    public String getCurrencyCodeValue() {
        return currencyCode == null ? null : currencyCode.value();
    }

    public String getOrderStatusValue() {
        return orderStatus == null ? null : orderStatus.value();
    }

    public String getTenantIdValue() {
        return tenantId == null ? null : String.valueOf(tenantId.value());
    }

    public String getOrderNoValue() {
        return orderNo == null ? null : orderNo.value();
    }

    public String getPayStatusValue() {
        return payStatus == null ? null : payStatus.value();
    }

    public String getInventoryStatusValue() {
        return inventoryStatus == null ? null : inventoryStatus.value();
    }

    public String getPaymentNoValue() {
        return paymentNo == null ? null : paymentNo.value();
    }

    public String getReservationNoValue() {
        return reservationNo == null ? null : reservationNo.value();
    }

    public String getPaymentChannelCodeValue() {
        return paymentChannelCode == null ? null : paymentChannelCode.value();
    }

    public void markReservingStock() {
        // 只有新建订单才能进入预占库存阶段，避免取消/关闭后的订单再次触发库存链路。
        ensureOrderStatus(OrderStatus.CREATED);
        this.orderStatus = OrderStatus.RESERVING_STOCK;
        this.inventoryStatus = InventoryStatus.RESERVING;
    }

    public String getWarehouseNoValue() {
        return warehouseNo == null ? null : warehouseNo.value();
    }

    public void markInventoryReserved(ReservationNo reservationNo, WarehouseNo warehouseNo) {
        // 预占成功只更新库存侧派生状态，主单仍停留在 RESERVING_STOCK，等待创建支付单后再切到待支付。
        ensureOrderStatus(OrderStatus.RESERVING_STOCK);
        this.reservationNo = reservationNo;
        this.warehouseNo = warehouseNo;
        this.inventoryStatus = InventoryStatus.RESERVED;
    }

    public void markInventoryReleased(ReservationNo reservationNo, WarehouseNo warehouseNo, String releaseReason, Instant releasedAt) {
        // 释放库存可能发生在取消、超时或支付失败之后，因此这里不再约束主单状态，只回写库存派生结果。
        this.reservationNo = reservationNo;
        this.warehouseNo = warehouseNo;
        this.inventoryReleaseReason = releaseReason;
        this.inventoryReleasedAt = releasedAt;
        this.inventoryStatus = InventoryStatus.RELEASED;
    }

    public void markInventoryDeducted(ReservationNo reservationNo, WarehouseNo warehouseNo, Instant deductedAt) {
        // 扣减库存同样属于支付成功后的派生结果回写，不反向改变订单主状态。
        this.reservationNo = reservationNo;
        this.warehouseNo = warehouseNo;
        this.inventoryDeductedAt = deductedAt;
        this.inventoryStatus = InventoryStatus.DEDUCTED;
    }

    public void markInventoryFailed(ReservationNo reservationNo, WarehouseNo warehouseNo, String failureReason) {
        // 这里记录的是库存侧最终失败事实，调用方会基于该结果决定是否补偿，不在实体内隐式关闭订单。
        this.reservationNo = reservationNo;
        this.warehouseNo = warehouseNo;
        this.inventoryFailureReason = failureReason;
        this.inventoryStatus = InventoryStatus.FAILED;
    }

    public void markPendingPayment(PaymentNo paymentNo, String channelCode) {
        // 订单只有完成库存预占后才能进入待支付，避免出现未锁库存就暴露支付入口。
        ensureOrderStatus(OrderStatus.RESERVING_STOCK);
        this.orderStatus = OrderStatus.PENDING_PAYMENT;
        this.payStatus = PayStatus.PAYING;
        this.paymentNo = paymentNo;
        this.paymentChannelCode = PaymentChannel.fromValue(channelCode);
    }

    public void markPaid(PaymentNo paymentNo, String channelCode, Money paidAmount, Instant paidTime) {
        // 订单支付成功是主状态终局之一，只允许从待支付进入，避免重复回调覆盖终态。
        ensureOrderStatus(OrderStatus.PENDING_PAYMENT);
        MoneyValidator.ensureSameCurrency(totalAmount, paidAmount);
        this.orderStatus = OrderStatus.PAID;
        this.payStatus = PayStatus.PAID;
        this.paymentNo = paymentNo;
        this.paymentChannelCode = PaymentChannel.fromValue(channelCode);
        this.paidAmount = paidAmount;
        this.paidAt = paidTime;
    }

    public void markPaymentFailed(PaymentNo paymentNo, String reason, String channelStatus, Instant failedAt) {
        // 支付失败会把主单直接收口为 CLOSED；后续库存释放只是派生补偿，不再改变主单终态。
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
        // 库存预占失败发生在支付创建之前，因此直接关闭订单，不保留待支付状态。
        ensureOrderStatus(OrderStatus.RESERVING_STOCK);
        this.orderStatus = OrderStatus.CLOSED;
        this.closeReason = reason;
        this.closedAt = Instant.now();
    }

    public void closeByPaymentCreateFailed(String reason) {
        // 创建支付单失败时主单仍在 RESERVING_STOCK；这里直接关单，后续由外部补偿释放已预占的库存。
        ensureOrderStatus(OrderStatus.RESERVING_STOCK);
        this.orderStatus = OrderStatus.CLOSED;
        this.closeReason = reason;
        this.closedAt = Instant.now();
    }

    public void closeExpired(String reason) {
        // 超时关闭允许覆盖“未开始预占”和“已生成支付单但未支付”两种场景，统一收口为 CLOSED/CLOSED。
        ensureOrderStatus(OrderStatus.CREATED, OrderStatus.PENDING_PAYMENT);
        this.orderStatus = OrderStatus.CLOSED;
        this.payStatus = PayStatus.CLOSED;
        this.closeReason = reason;
        this.closedAt = Instant.now();
    }

    public void cancel(String reason) {
        // 用户取消比超时更宽松，允许在库存预占中止损；库存释放由调用方根据当前派生状态决定是否执行。
        ensureOrderStatus(OrderStatus.CREATED, OrderStatus.PENDING_PAYMENT, OrderStatus.RESERVING_STOCK);
        this.orderStatus = OrderStatus.CANCELLED;
        this.payStatus = PayStatus.CLOSED;
        this.cancelReason = reason;
        this.closedAt = Instant.now();
    }

    private void ensureOrderStatus(OrderStatus... expectedStatuses) {
        // 订单实体只负责守住主状态机，不在这里做幂等静默；上层应用服务应先决定是否短路。
        for (OrderStatus expectedStatus : expectedStatuses) {
            if (expectedStatus == orderStatus) {
                return;
            }
        }
        throw new IllegalStateException("Invalid order status: " + getOrderStatusValue());
    }

}
