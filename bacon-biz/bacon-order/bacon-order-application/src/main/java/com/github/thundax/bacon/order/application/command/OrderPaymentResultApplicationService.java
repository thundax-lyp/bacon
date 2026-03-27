package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.order.application.executor.OrderIdempotencyExecutor;
import com.github.thundax.bacon.order.application.support.OrderDerivedDataPersistenceSupport;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class OrderPaymentResultApplicationService {

    private static final String ACTION_MARK_PAID = "ORDER_MARK_PAID";
    private static final String ACTION_MARK_PAYMENT_FAILED = "ORDER_MARK_PAYMENT_FAILED";

    private final OrderRepository orderRepository;
    private final InventoryCommandFacade inventoryCommandFacade;
    private final OrderIdempotencyExecutor orderIdempotencyExecutor;
    private final OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport;

    public OrderPaymentResultApplicationService(OrderRepository orderRepository,
                                                InventoryCommandFacade inventoryCommandFacade,
                                                OrderIdempotencyExecutor orderIdempotencyExecutor,
                                                OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport) {
        this.orderRepository = orderRepository;
        this.inventoryCommandFacade = inventoryCommandFacade;
        this.orderIdempotencyExecutor = orderIdempotencyExecutor;
        this.orderDerivedDataPersistenceSupport = orderDerivedDataPersistenceSupport;
    }

    public void markPaid(Long tenantId, String orderNo, String paymentNo, String channelCode, BigDecimal paidAmount,
                         Instant paidTime) {
        // 支付成功回写是跨域回调入口，必须走幂等执行器，避免同一支付结果被重复扣减库存。
        orderIdempotencyExecutor.execute(OrderIdempotencyExecutor.EVENT_MARK_PAID, tenantId, orderNo, paymentNo,
                () -> doMarkPaid(tenantId, orderNo, paymentNo, channelCode, paidAmount, paidTime));
    }

    public void markPaymentFailed(Long tenantId, String orderNo, String paymentNo, String reason, String channelStatus,
                                  Instant failedTime) {
        // 支付失败回写同样需要幂等，避免重复失败通知把释放库存动作执行多次。
        orderIdempotencyExecutor.execute(OrderIdempotencyExecutor.EVENT_MARK_PAYMENT_FAILED, tenantId, orderNo,
                paymentNo, () -> doMarkPaymentFailed(tenantId, orderNo, paymentNo, reason, channelStatus, failedTime));
    }

    private void doMarkPaid(Long tenantId, String orderNo, String paymentNo, String channelCode,
                            BigDecimal paidAmount, Instant paidTime) {
        Order order = orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        String beforeStatus = order.getOrderStatus();
        order.markPaid(paymentNo, channelCode, paidAmount, paidTime);
        // 支付成功后库存扣减是硬前置条件；如果扣减失败，直接抛错让幂等和重试链路接管，避免订单看起来已完成但库存未落账。
        InventoryReservationResultDTO deductResult = inventoryCommandFacade.deductReservedStock(tenantId, orderNo);
        if (!Order.INVENTORY_STATUS_DEDUCTED.equals(deductResult.getInventoryStatus())) {
            String reason = resolveFailureReason(deductResult.getFailureReason(), "inventory deduct failed");
            order.markInventoryFailed(deductResult.getReservationNo(), deductResult.getWarehouseId(), reason);
            orderRepository.save(order);
            throw new IllegalStateException(reason);
        }
        order.markInventoryDeducted(deductResult.getReservationNo(), deductResult.getWarehouseId(),
                deductResult.getDeductedAt());
        orderRepository.save(order);
        orderDerivedDataPersistenceSupport.persist(order, ACTION_MARK_PAID, beforeStatus);
    }

    private void doMarkPaymentFailed(Long tenantId, String orderNo, String paymentNo, String reason,
                                     String channelStatus, Instant failedTime) {
        Order order = orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
        String beforeStatus = order.getOrderStatus();
        order.markPaymentFailed(paymentNo, reason, channelStatus, failedTime);
        // 支付失败后的主目标是回收预占库存，因此这里固定走 releaseReservedStock，而不是尝试别的库存路径。
        InventoryReservationResultDTO releaseResult =
                inventoryCommandFacade.releaseReservedStock(tenantId, orderNo, "PAYMENT_FAILED");
        applyReleaseResult(order, releaseResult, "PAYMENT_FAILED");
        orderRepository.save(order);
        orderDerivedDataPersistenceSupport.persist(order, ACTION_MARK_PAYMENT_FAILED, beforeStatus);
    }

    private void applyReleaseResult(Order order, InventoryReservationResultDTO releaseResult, String fallbackReason) {
        if (Order.INVENTORY_STATUS_RELEASED.equals(releaseResult.getInventoryStatus())) {
            order.markInventoryReleased(releaseResult.getReservationNo(), releaseResult.getWarehouseId(),
                    releaseResult.getReleaseReason(), releaseResult.getReleasedAt());
            return;
        }
        order.markInventoryFailed(releaseResult.getReservationNo(), releaseResult.getWarehouseId(),
                resolveFailureReason(releaseResult.getFailureReason(), fallbackReason));
    }

    private String resolveFailureReason(String reason, String defaultReason) {
        return reason == null || reason.isBlank() ? defaultReason : reason;
    }
}
