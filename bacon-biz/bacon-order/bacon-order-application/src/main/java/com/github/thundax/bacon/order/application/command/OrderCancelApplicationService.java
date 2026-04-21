package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
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
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import com.github.thundax.bacon.payment.api.request.PaymentCloseFacadeRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void cancel(OrderCancelCommand command) {
        BaconContextHolder.requireTenantId();
        String resolvedReason = command.reason() == null || command.reason().isBlank() ? "USER_CANCELLED" : command.reason();
        // 取消订单走幂等执行器，避免用户重复点击或上游重复投递时把关单/释放库存执行多次。
        orderIdempotencyExecutor.execute(
                OrderIdempotencyExecutor.EVENT_CANCEL,
                OrderNoCodec.toValue(command.orderNo()),
                () -> doCancel(command.orderNo(), resolvedReason));
    }

    private void doCancel(OrderNo orderNo, String reason) {
        Order order = orderRepository
                .findByOrderNo(orderNo)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderNo));
        OrderStatus beforeStatus = order.getOrderStatus();
        order.cancel(reason);
        // 同步主流程里先改订单主状态，再尝试释放库存和关闭支付；即使后续远程动作部分失败，主单也已明确进入取消态。
        InventoryReservationFacadeResponse releaseResult = inventoryCommandFacade.releaseReservedStock(
                new InventoryReleaseFacadeRequest(OrderNoCodec.toValue(orderNo), reason));
        applyReleaseResult(order, releaseResult, reason);
        if (order.getPaymentNo() != null && !order.getPaymentNo().value().isBlank()) {
            paymentCommandFacade.closePayment(new PaymentCloseFacadeRequest(order.getPaymentNo().value(), reason));
        }
        orderRepository.update(order);
        orderDerivedDataPersistenceSupport.persist(order, ACTION_CANCEL, beforeStatus);
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
