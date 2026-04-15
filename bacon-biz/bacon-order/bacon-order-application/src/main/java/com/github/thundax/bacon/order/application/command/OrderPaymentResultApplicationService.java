package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.commerce.codec.PaymentNoCodec;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.order.application.codec.ReservationNoCodec;
import com.github.thundax.bacon.order.application.executor.OrderIdempotencyExecutor;
import com.github.thundax.bacon.order.application.support.OrderDerivedDataPersistenceSupport;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderAuditActionType;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class OrderPaymentResultApplicationService {

    private static final OrderAuditActionType ACTION_MARK_PAID = OrderAuditActionType.ORDER_MARK_PAID;
    private static final OrderAuditActionType ACTION_MARK_PAYMENT_FAILED =
            OrderAuditActionType.ORDER_MARK_PAYMENT_FAILED;

    private final OrderRepository orderRepository;
    private final InventoryCommandFacade inventoryCommandFacade;
    private final OrderIdempotencyExecutor orderIdempotencyExecutor;
    private final OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport;

    public OrderPaymentResultApplicationService(
            OrderRepository orderRepository,
            InventoryCommandFacade inventoryCommandFacade,
            OrderIdempotencyExecutor orderIdempotencyExecutor,
            OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport) {
        this.orderRepository = orderRepository;
        this.inventoryCommandFacade = inventoryCommandFacade;
        this.orderIdempotencyExecutor = orderIdempotencyExecutor;
        this.orderDerivedDataPersistenceSupport = orderDerivedDataPersistenceSupport;
    }

    public void markPaid(
            OrderNo orderNo, PaymentNo paymentNo, String channelCode, BigDecimal paidAmount, Instant paidTime) {
        BaconContextHolder.requireTenantId();
        // 支付成功回写是跨域回调入口，必须走幂等执行器，避免同一支付结果被重复扣减库存。
        orderIdempotencyExecutor.execute(
                OrderIdempotencyExecutor.EVENT_MARK_PAID,
                OrderNoCodec.toValue(orderNo),
                PaymentNoCodec.toValue(paymentNo),
                () -> doMarkPaid(orderNo, paymentNo, channelCode, paidAmount, paidTime));
    }

    public void markPaymentFailed(
            OrderNo orderNo, PaymentNo paymentNo, String reason, String channelStatus, Instant failedTime) {
        BaconContextHolder.requireTenantId();
        // 支付失败回写同样需要幂等，避免重复失败通知把释放库存动作执行多次。
        orderIdempotencyExecutor.execute(
                OrderIdempotencyExecutor.EVENT_MARK_PAYMENT_FAILED,
                OrderNoCodec.toValue(orderNo),
                PaymentNoCodec.toValue(paymentNo),
                () -> doMarkPaymentFailed(orderNo, paymentNo, reason, channelStatus, failedTime));
    }

    private void doMarkPaid(
            OrderNo orderNo, PaymentNo paymentNo, String channelCode, BigDecimal paidAmount, Instant paidTime) {
        Order order = orderRepository
                .findByOrderNo(OrderNoCodec.toValue(orderNo))
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        OrderStatus beforeStatus = order.getOrderStatus();
        order.markPaid(paymentNo, channelCode, Money.of(paidAmount, order.getCurrencyCode()), paidTime);
        // 支付成功后库存扣减是硬前置条件；如果扣减失败，直接抛错让幂等和重试链路接管，避免订单看起来已完成但库存未落账。
        InventoryReservationResultDTO deductResult =
                inventoryCommandFacade.deductReservedStock(OrderNoCodec.toValue(orderNo));
        if (!InventoryStatus.DEDUCTED.value().equals(deductResult.getInventoryStatus())) {
            String reason = deductResult.getFailureReason() == null
                            || deductResult.getFailureReason().isBlank()
                    ? "inventory deduct failed"
                    : deductResult.getFailureReason();
            order.markInventoryFailed(
                    ReservationNoCodec.toDomain(deductResult.getReservationNo()),
                    deductResult.getWarehouseCode() == null ? null : WarehouseCode.of(deductResult.getWarehouseCode()),
                    reason);
            orderRepository.save(order);
            throw new IllegalStateException(reason);
        }
        order.markInventoryDeducted(
                ReservationNoCodec.toDomain(deductResult.getReservationNo()),
                deductResult.getWarehouseCode() == null ? null : WarehouseCode.of(deductResult.getWarehouseCode()),
                deductResult.getDeductedAt());
        orderRepository.save(order);
        orderDerivedDataPersistenceSupport.persist(order, ACTION_MARK_PAID, beforeStatus);
    }

    private void doMarkPaymentFailed(
            OrderNo orderNo, PaymentNo paymentNo, String reason, String channelStatus, Instant failedTime) {
        Order order = orderRepository
                .findByOrderNo(OrderNoCodec.toValue(orderNo))
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        OrderStatus beforeStatus = order.getOrderStatus();
        order.markPaymentFailed(paymentNo, reason, channelStatus, failedTime);
        // 支付失败后的主目标是回收预占库存，因此这里固定走 releaseReservedStock，而不是尝试别的库存路径。
        InventoryReservationResultDTO releaseResult =
                inventoryCommandFacade.releaseReservedStock(OrderNoCodec.toValue(orderNo), "PAYMENT_FAILED");
        applyReleaseResult(order, releaseResult, "PAYMENT_FAILED");
        orderRepository.save(order);
        orderDerivedDataPersistenceSupport.persist(order, ACTION_MARK_PAYMENT_FAILED, beforeStatus);
    }

    private void applyReleaseResult(Order order, InventoryReservationResultDTO releaseResult, String fallbackReason) {
        if (InventoryStatus.RELEASED.value().equals(releaseResult.getInventoryStatus())) {
            order.markInventoryReleased(
                    ReservationNoCodec.toDomain(releaseResult.getReservationNo()),
                    releaseResult.getWarehouseCode() == null
                            ? null
                            : WarehouseCode.of(releaseResult.getWarehouseCode()),
                    releaseResult.getReleaseReason(),
                    releaseResult.getReleasedAt());
            return;
        }
        order.markInventoryFailed(
                ReservationNoCodec.toDomain(releaseResult.getReservationNo()),
                releaseResult.getWarehouseCode() == null ? null : WarehouseCode.of(releaseResult.getWarehouseCode()),
                releaseResult.getFailureReason() == null
                                || releaseResult.getFailureReason().isBlank()
                        ? fallbackReason
                        : releaseResult.getFailureReason());
    }
}
