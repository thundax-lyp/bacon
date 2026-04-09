package com.github.thundax.bacon.order.application.saga;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.application.codec.OrderOutboxPayloadCodec;
import com.github.thundax.bacon.order.application.support.OrderDerivedDataPersistenceSupport;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxStatus;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderAuditActionType;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxRepository;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.model.valueobject.PaymentNo;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OrderOutboxActionExecutor {

    private static final String CLOSE_REASON_INVENTORY_RESERVE_FAILED = "INVENTORY_RESERVE_FAILED";
    private static final String CLOSE_REASON_PAYMENT_CREATE_FAILED = "PAYMENT_CREATE_FAILED";

    private final OrderRepository orderRepository;
    private final OrderOutboxRepository orderOutboxRepository;
    private final InventoryCommandFacade inventoryCommandFacade;
    private final PaymentCommandFacade paymentCommandFacade;
    private final OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport;

    public OrderOutboxActionExecutor(OrderRepository orderRepository,
                                     OrderOutboxRepository orderOutboxRepository,
                                     InventoryCommandFacade inventoryCommandFacade,
                                     PaymentCommandFacade paymentCommandFacade,
                                     OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport) {
        this.orderRepository = orderRepository;
        this.orderOutboxRepository = orderOutboxRepository;
        this.inventoryCommandFacade = inventoryCommandFacade;
        this.paymentCommandFacade = paymentCommandFacade;
        this.orderDerivedDataPersistenceSupport = orderDerivedDataPersistenceSupport;
    }

    public void enqueueReserveStock(Long tenantId, String orderNo, String channelCode) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("channelCode", channelCode == null ? "MOCK" : channelCode);
        orderOutboxRepository.saveOutboxEvent(new OrderOutboxEvent(null, null, tenantId, orderNo,
                OrderOutboxEventType.RESERVE_STOCK,
                tenantId + ":" + orderNo + ":RESERVE", OrderOutboxPayloadCodec.encode(payload),
                OrderOutboxStatus.NEW, 0, null, null, null, null, null,
                null, Instant.now(), Instant.now()));
    }

    public void executeClaimed(OrderOutboxEvent event) {
        // Outbox 事件是订单创建链路的异步阶段机，事件类型决定当前推进到哪一步。
        if (OrderOutboxEventType.RESERVE_STOCK == event.getEventType()) {
            executeReserveStock(event);
            return;
        }
        if (OrderOutboxEventType.CREATE_PAYMENT == event.getEventType()) {
            executeCreatePayment(event);
            return;
        }
        if (OrderOutboxEventType.RELEASE_STOCK == event.getEventType()) {
            executeReleaseStock(event);
            return;
        }
        throw new IllegalStateException("Unsupported outbox event type: " + event.getEventType());
    }

    private void executeReserveStock(OrderOutboxEvent event) {
        Order order = findOrder(event.getTenantIdValue(), event.getOrderNoValue());
        List<OrderItem> items = orderRepository.findItemsByOrderId(order.getTenantIdValue(), order.getIdValue(),
                order.getCurrencyCodeValue());
        List<InventoryReservationItemDTO> reserveItems = items.stream()
                .map(item -> new InventoryReservationItemDTO(item.getSkuId() == null ? null : item.getSkuId().value(),
                        item.getQuantity()))
                .toList();
        InventoryReservationResultDTO reserveResult = inventoryCommandFacade.reserveStock(order.getTenantIdValue(),
                order.getOrderNoValue(), reserveItems);
        // 预占失败时直接把订单收敛为关闭态，不再继续创建支付单，避免出现“无库存但有支付单”的脏状态。
        if (!InventoryStatus.RESERVED.value().equals(reserveResult.getInventoryStatus())) {
            String reason = resolveFailureReason(reserveResult.getFailureReason(), "inventory reserve failed");
            order.markInventoryFailed(toReservationNo(reserveResult.getReservationNo()),
                    toWarehouseCode(reserveResult.getWarehouseCode()), reason);
            order.closeByInventoryReserveFailed(CLOSE_REASON_INVENTORY_RESERVE_FAILED);
            orderRepository.save(order);
            orderDerivedDataPersistenceSupport.persist(order, OrderAuditActionType.OUTBOX_RESERVE_FAILED,
                    OrderStatus.RESERVING_STOCK);
            return;
        }
        order.markInventoryReserved(toReservationNo(reserveResult.getReservationNo()),
                toWarehouseCode(reserveResult.getWarehouseCode()));
        orderRepository.save(order);
        orderDerivedDataPersistenceSupport.persist(order, OrderAuditActionType.OUTBOX_RESERVE_OK,
                OrderStatus.RESERVING_STOCK);

        // 只有库存预占成功后才补发创建支付事件，确保支付链路依赖的库存前置条件已经成立。
        Map<String, String> source = OrderOutboxPayloadCodec.decode(event.getPayload());
        String channelCode = source.getOrDefault("channelCode", "MOCK");
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("channelCode", channelCode);
        orderOutboxRepository.saveOutboxEvent(new OrderOutboxEvent(null, null, order.getTenantIdValue(), order.getOrderNoValue(),
                OrderOutboxEventType.CREATE_PAYMENT,
                order.getTenantIdValue() + ":" + order.getOrderNoValue() + ":CREATE_PAYMENT",
                OrderOutboxPayloadCodec.encode(payload), OrderOutboxStatus.NEW, 0, null, null, null,
                null, null, null, Instant.now(), Instant.now()));
    }

    private void executeCreatePayment(OrderOutboxEvent event) {
        Order order = findOrder(event.getTenantIdValue(), event.getOrderNoValue());
        Map<String, String> payload = OrderOutboxPayloadCodec.decode(event.getPayload());
        String channelCode = payload.getOrDefault("channelCode", "MOCK");
        PaymentCreateResultDTO paymentResult = paymentCommandFacade.createPayment(order.getTenantIdValue(), order.getOrderNoValue(),
                order.getUserId() == null ? null : order.getUserId().value(), order.getPayableAmount().value(),
                channelCode, "order:" + order.getOrderNoValue(),
                order.getExpiredAt());
        // 创建支付单失败时不只关闭订单，还要补一条释放库存事件，把前一步已预占的资源回收掉。
        if (paymentResult.getPaymentNo() == null || paymentResult.getPaymentNo().isBlank()
                || !PayStatus.PAYING.value().equals(paymentResult.getPaymentStatus())) {
            order.closeByPaymentCreateFailed(CLOSE_REASON_PAYMENT_CREATE_FAILED);
            orderRepository.save(order);
            orderDerivedDataPersistenceSupport.persist(order, OrderAuditActionType.OUTBOX_CREATE_PAYMENT_FAILED,
                    OrderStatus.RESERVING_STOCK);

            Map<String, String> releasePayload = new LinkedHashMap<>();
            releasePayload.put("reason", CLOSE_REASON_PAYMENT_CREATE_FAILED);
            orderOutboxRepository.saveOutboxEvent(new OrderOutboxEvent(null, null, order.getTenantIdValue(), order.getOrderNoValue(),
                    OrderOutboxEventType.RELEASE_STOCK,
                    order.getTenantIdValue() + ":" + order.getOrderNoValue() + ":RELEASE_PAYMENT_CREATE_FAILED",
                    OrderOutboxPayloadCodec.encode(releasePayload), OrderOutboxStatus.NEW, 0, null,
                    null, null, null, null, null, Instant.now(), Instant.now()));
            return;
        }
        order.markPendingPayment(PaymentNo.of(paymentResult.getPaymentNo()), paymentResult.getChannelCode());
        orderRepository.save(order);
        orderDerivedDataPersistenceSupport.persist(order, OrderAuditActionType.OUTBOX_CREATE_PAYMENT_OK,
                OrderStatus.RESERVING_STOCK);
    }

    private void executeReleaseStock(OrderOutboxEvent event) {
        Order order = findOrder(event.getTenantIdValue(), event.getOrderNoValue());
        String reason = OrderOutboxPayloadCodec.decode(event.getPayload()).getOrDefault("reason", "SYSTEM_CANCELLED");
        InventoryReservationResultDTO releaseResult = inventoryCommandFacade.releaseReservedStock(order.getTenantIdValue(),
                order.getOrderNoValue(), reason);
        // 释放结果只更新库存侧派生状态，不再反向改订单主状态；订单主状态在上游取消/超时/支付失败时已经确定。
        if (InventoryStatus.RELEASED.value().equals(releaseResult.getInventoryStatus())) {
            order.markInventoryReleased(toReservationNo(releaseResult.getReservationNo()),
                    toWarehouseCode(releaseResult.getWarehouseCode()),
                    releaseResult.getReleaseReason(), releaseResult.getReleasedAt());
        } else {
            order.markInventoryFailed(toReservationNo(releaseResult.getReservationNo()),
                    toWarehouseCode(releaseResult.getWarehouseCode()),
                    resolveFailureReason(releaseResult.getFailureReason(), reason));
        }
        orderRepository.save(order);
        orderDerivedDataPersistenceSupport.persist(order, OrderAuditActionType.OUTBOX_RELEASE, order.getOrderStatus());
    }

    private Order findOrder(Long tenantId, String orderNo) {
        return orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
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

    private TenantId toTenantId(Long tenantId) {
        return tenantId == null ? null : TenantId.of(tenantId);
    }

    private OrderNo toOrderNo(String orderNo) {
        return orderNo == null ? null : OrderNo.of(orderNo);
    }
}
