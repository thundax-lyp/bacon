package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.inventory.api.request.InventoryDeductFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReleaseFacadeRequest;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;
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
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void markPaid(
            OrderNo orderNo, PaymentNo paymentNo, String channelCode, BigDecimal paidAmount, Instant paidTime) {
        BaconContextHolder.requireTenantId();
        // 支付成功回写是跨域回调入口，必须走幂等执行器，避免同一支付结果被重复扣减库存。
        orderIdempotencyExecutor.execute(
                OrderIdempotencyExecutor.EVENT_MARK_PAID,
                OrderNoCodec.toValue(orderNo),
                () -> doMarkPaid(orderNo, paymentNo, channelCode, paidAmount, paidTime));
    }

    @Transactional
    public void markPaymentFailed(
            OrderNo orderNo, PaymentNo paymentNo, String reason, String channelStatus, Instant failedTime) {
        BaconContextHolder.requireTenantId();
        // 支付失败回写同样需要幂等，避免重复失败通知把释放库存动作执行多次。
        orderIdempotencyExecutor.execute(
                OrderIdempotencyExecutor.EVENT_MARK_PAYMENT_FAILED,
                OrderNoCodec.toValue(orderNo),
                () -> doMarkPaymentFailed(orderNo, paymentNo, reason, channelStatus, failedTime));
    }

    private void doMarkPaid(
            OrderNo orderNo, PaymentNo paymentNo, String channelCode, BigDecimal paidAmount, Instant paidTime) {
        Order order = orderRepository
                .findByOrderNo(orderNo)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderNo));
        OrderStatus beforeStatus = order.getOrderStatus();
        order.markPaid(paymentNo, channelCode, Money.of(paidAmount, order.getCurrencyCode()), paidTime);
        InventoryReservationFacadeResponse deductResult = inventoryCommandFacade.deductReservedStock(
                new InventoryDeductFacadeRequest(OrderNoCodec.toValue(orderNo)));
        boolean deducted = order.recordInventoryDeductionResult(
                toInventoryStatus(deductResult.getInventoryStatus()),
                ReservationNoCodec.toDomain(deductResult.getReservationNo()),
                toWarehouseCode(deductResult.getWarehouseCode()),
                deductResult.getDeductedAt(),
                resolveReason(deductResult.getFailureReason(), "inventory deduct failed"));
        orderRepository.update(order);
        if (!deducted) {
            throw new IllegalStateException(resolveReason(deductResult.getFailureReason(), "inventory deduct failed"));
        }
        orderDerivedDataPersistenceSupport.persist(order, ACTION_MARK_PAID, beforeStatus);
    }

    private void doMarkPaymentFailed(
            OrderNo orderNo, PaymentNo paymentNo, String reason, String channelStatus, Instant failedTime) {
        Order order = orderRepository
                .findByOrderNo(orderNo)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderNo));
        OrderStatus beforeStatus = order.getOrderStatus();
        order.markPaymentFailed(paymentNo, reason, channelStatus, failedTime);
        // 支付失败后的主目标是回收预占库存，因此这里固定走 releaseReservedStock，而不是尝试别的库存路径。
        InventoryReservationFacadeResponse releaseResult = inventoryCommandFacade.releaseReservedStock(
                new InventoryReleaseFacadeRequest(OrderNoCodec.toValue(orderNo), "PAYMENT_FAILED"));
        applyReleaseResult(order, releaseResult, "PAYMENT_FAILED");
        orderRepository.update(order);
        orderDerivedDataPersistenceSupport.persist(order, ACTION_MARK_PAYMENT_FAILED, beforeStatus);
    }

    private void applyReleaseResult(Order order, InventoryReservationFacadeResponse releaseResult, String fallbackReason) {
        order.recordInventoryReleaseResult(
                toInventoryStatus(releaseResult.getInventoryStatus()),
                ReservationNoCodec.toDomain(releaseResult.getReservationNo()),
                toWarehouseCode(releaseResult.getWarehouseCode()),
                releaseResult.getReleaseReason(),
                releaseResult.getReleasedAt(),
                resolveReason(releaseResult.getFailureReason(), fallbackReason));
    }

    private InventoryStatus toInventoryStatus(String inventoryStatus) {
        return inventoryStatus == null || inventoryStatus.isBlank() ? null : InventoryStatus.from(inventoryStatus);
    }

    private WarehouseCode toWarehouseCode(String warehouseCode) {
        return warehouseCode == null || warehouseCode.isBlank() ? null : WarehouseCode.of(warehouseCode);
    }

    private String resolveReason(String reason, String fallbackReason) {
        return reason == null || reason.isBlank() ? fallbackReason : reason;
    }
}
