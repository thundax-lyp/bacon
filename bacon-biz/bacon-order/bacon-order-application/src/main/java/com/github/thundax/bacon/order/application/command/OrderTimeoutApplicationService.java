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

@Service
public class OrderTimeoutApplicationService {

    private static final OrderAuditActionType ACTION_CLOSE_EXPIRED = OrderAuditActionType.ORDER_CLOSE_EXPIRED;

    private final OrderRepository orderRepository;
    private final InventoryCommandFacade inventoryCommandFacade;
    private final PaymentCommandFacade paymentCommandFacade;
    private final OrderIdempotencyExecutor orderIdempotencyExecutor;
    private final OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport;

    public OrderTimeoutApplicationService(
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

    public void closeExpiredOrder(OrderNo orderNo, String reason) {
        BaconContextHolder.requireTenantId();
        // 超时关单同样走幂等执行器，防止定时任务重复扫描时对同一订单反复关单和释放资源。
        orderIdempotencyExecutor.execute(
                OrderIdempotencyExecutor.EVENT_CLOSE_EXPIRED,
                OrderNoCodec.toValue(orderNo),
                null,
                () -> doCloseExpiredOrder(orderNo, reason));
    }

    private void doCloseExpiredOrder(OrderNo orderNo, String reason) {
        Order order = orderRepository
                .findByOrderNo(OrderNoCodec.toValue(orderNo))
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderNo));
        OrderStatus beforeStatus = order.getOrderStatus();
        order.closeExpired(reason);
        // 超时关单的资源回收顺序固定为“先关支付，再释放库存”，与订单生命周期的依赖方向保持一致。
        if (order.getPaymentNo() != null && !order.getPaymentNo().value().isBlank()) {
            paymentCommandFacade.closePayment(new PaymentCloseFacadeRequest(order.getPaymentNo().value(), reason));
        }
        InventoryReservationFacadeResponse releaseResult = inventoryCommandFacade.releaseReservedStock(
                new InventoryReleaseFacadeRequest(OrderNoCodec.toValue(orderNo), reason));
        applyReleaseResult(order, releaseResult, reason);
        orderRepository.updateOrder(order);
        orderDerivedDataPersistenceSupport.persist(order, ACTION_CLOSE_EXPIRED, beforeStatus);
    }

    private void applyReleaseResult(Order order, InventoryReservationFacadeResponse releaseResult, String fallbackReason) {
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
        // 库存释放异常只体现在派生状态上，主订单仍保持 CLOSED，等待后续补偿或人工处理。
        order.markInventoryFailed(
                ReservationNoCodec.toDomain(releaseResult.getReservationNo()),
                releaseResult.getWarehouseCode() == null ? null : WarehouseCode.of(releaseResult.getWarehouseCode()),
                releaseResult.getFailureReason() == null
                                || releaseResult.getFailureReason().isBlank()
                        ? fallbackReason
                        : releaseResult.getFailureReason());
    }
}
