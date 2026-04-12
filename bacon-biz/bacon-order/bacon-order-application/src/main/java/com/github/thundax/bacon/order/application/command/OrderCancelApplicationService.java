package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.order.application.executor.OrderIdempotencyExecutor;
import com.github.thundax.bacon.order.application.support.OrderDerivedDataPersistenceSupport;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderAuditActionType;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import org.springframework.stereotype.Service;

@Service
public class OrderCancelApplicationService {

    private static final OrderAuditActionType ACTION_CANCEL = OrderAuditActionType.ORDER_CANCEL;

    private final OrderRepository orderRepository;
    private final InventoryCommandFacade inventoryCommandFacade;
    private final PaymentCommandFacade paymentCommandFacade;
    private final OrderIdempotencyExecutor orderIdempotencyExecutor;
    private final OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport;

    public OrderCancelApplicationService(
            OrderRepository orderRepository,
            InventoryCommandFacade inventoryCommandFacade,
            PaymentCommandFacade paymentCommandFacade,
            OrderIdempotencyExecutor orderIdempotencyExecutor,
            OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport) {
        this.orderRepository = orderRepository;
        this.inventoryCommandFacade = inventoryCommandFacade;
        this.paymentCommandFacade = paymentCommandFacade;
        this.orderIdempotencyExecutor = orderIdempotencyExecutor;
        this.orderDerivedDataPersistenceSupport = orderDerivedDataPersistenceSupport;
    }

    public void cancel(Long tenantId, String orderNo, String reason) {
        String resolvedReason = reason == null || reason.isBlank() ? "USER_CANCELLED" : reason;
        // 取消订单走幂等执行器，避免用户重复点击或上游重复投递时把关单/释放库存执行多次。
        orderIdempotencyExecutor.execute(
                OrderIdempotencyExecutor.EVENT_CANCEL,
                tenantId,
                orderNo,
                null,
                () -> doCancel(tenantId, orderNo, resolvedReason));
    }

    private void doCancel(Long tenantId, String orderNo, String reason) {
        Order order = orderRepository
                .findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        OrderStatus beforeStatus = order.getOrderStatus();
        order.cancel(reason);
        // 同步主流程里先改订单主状态，再尝试释放库存和关闭支付；即使后续远程动作部分失败，主单也已明确进入取消态。
        InventoryReservationResultDTO releaseResult = BaconContextHolder.callWithTenantId(
                tenantId, () -> inventoryCommandFacade.releaseReservedStock(orderNo, reason));
        applyReleaseResult(order, releaseResult, reason);
        if (order.getPaymentNoValue() != null && !order.getPaymentNoValue().isBlank()) {
            BaconContextHolder.runWithTenantId(
                    tenantId, () -> paymentCommandFacade.closePayment(order.getPaymentNoValue(), reason));
        }
        orderRepository.save(order);
        orderDerivedDataPersistenceSupport.persist(order, ACTION_CANCEL, beforeStatus);
    }

    private void applyReleaseResult(Order order, InventoryReservationResultDTO releaseResult, String fallbackReason) {
        if (InventoryStatus.RELEASED.value().equals(releaseResult.getInventoryStatus())) {
            order.markInventoryReleased(
                    toReservationNo(releaseResult.getReservationNo()),
                    toWarehouseCode(releaseResult.getWarehouseCode()),
                    releaseResult.getReleaseReason(),
                    releaseResult.getReleasedAt());
            return;
        }
        // 释放失败只更新库存派生状态，方便后续排障或补偿，不会把已经确定的取消主状态回滚掉。
        order.markInventoryFailed(
                toReservationNo(releaseResult.getReservationNo()),
                toWarehouseCode(releaseResult.getWarehouseCode()),
                resolveFailureReason(releaseResult.getFailureReason(), fallbackReason));
    }

    private String resolveFailureReason(String reason, String defaultReason) {
        return reason == null || reason.isBlank() ? defaultReason : reason;
    }

    private ReservationNo toReservationNo(String reservationNo) {
        return reservationNo == null ? null : ReservationNo.of(reservationNo);
    }

    private WarehouseCode toWarehouseCode(String warehouseCode) {
        return warehouseCode == null ? null : WarehouseCode.of(warehouseCode);
    }
}
