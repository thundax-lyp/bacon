package com.github.thundax.bacon.order.application.saga;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.inventory.api.request.InventoryReleaseFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReservationItemFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReserveFacadeRequest;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;
import com.github.thundax.bacon.common.commerce.codec.SkuIdCodec;
import com.github.thundax.bacon.order.application.codec.OrderOutboxPayloadCodec;
import com.github.thundax.bacon.order.application.codec.ReservationNoCodec;
import com.github.thundax.bacon.order.application.support.OrderDerivedDataPersistenceSupport;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderAuditActionType;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxRepository;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import com.github.thundax.bacon.payment.api.request.PaymentCreateFacadeRequest;
import com.github.thundax.bacon.payment.api.response.PaymentCreateFacadeResponse;
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

    public OrderOutboxActionExecutor(
            OrderRepository orderRepository,
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

    public void enqueueReserveStock(String orderNo, String channelCode) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("channelCode", channelCode == null ? "MOCK" : channelCode);
        orderOutboxRepository.insert(OrderOutboxEvent.create(
                orderNo,
                OrderOutboxEventType.RESERVE_STOCK,
                orderNo + ":RESERVE",
                OrderOutboxPayloadCodec.encode(payload),
                OrderOutboxStatus.NEW,
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                Instant.now()));
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
        Order order = findOrder(event.getOrderNo());
        List<OrderItem> items = orderRepository.listItemsByOrderId(order.getId());
        List<InventoryReservationItemFacadeRequest> reserveItems = items.stream()
                .map(item -> new InventoryReservationItemFacadeRequest(SkuIdCodec.toValue(item.getSkuId()), item.getQuantity()))
                .toList();
        InventoryReservationFacadeResponse reserveResult = BaconContextHolder.callWithTenantId(
                BaconContextHolder.requireTenantId(),
                () -> inventoryCommandFacade.reserveStock(new InventoryReserveFacadeRequest(
                        order.getOrderNo() == null ? null : order.getOrderNo().value(), reserveItems)));
        boolean reserved = order.recordInventoryReservationResult(
                toInventoryStatus(reserveResult.getInventoryStatus()),
                ReservationNoCodec.toDomain(reserveResult.getReservationNo()),
                toWarehouseCode(reserveResult.getWarehouseCode()),
                resolveReason(reserveResult.getFailureReason(), "inventory reserve failed"),
                CLOSE_REASON_INVENTORY_RESERVE_FAILED);
        orderRepository.update(order);
        if (!reserved) {
            orderDerivedDataPersistenceSupport.persist(
                    order, OrderAuditActionType.OUTBOX_RESERVE_FAILED, OrderStatus.RESERVING_STOCK);
            return;
        }
        orderDerivedDataPersistenceSupport.persist(
                order, OrderAuditActionType.OUTBOX_RESERVE_OK, OrderStatus.RESERVING_STOCK);

        // 只有库存预占成功后才补发创建支付事件，确保支付链路依赖的库存前置条件已经成立。
        Map<String, String> source = OrderOutboxPayloadCodec.decode(event.getPayload());
        String channelCode = source.getOrDefault("channelCode", "MOCK");
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("channelCode", channelCode);
        orderOutboxRepository.insert(OrderOutboxEvent.create(
                order.getOrderNo() == null ? null : order.getOrderNo().value(),
                OrderOutboxEventType.CREATE_PAYMENT,
                (order.getOrderNo() == null ? null : order.getOrderNo().value()) + ":CREATE_PAYMENT",
                OrderOutboxPayloadCodec.encode(payload),
                OrderOutboxStatus.NEW,
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                Instant.now()));
    }

    private void executeCreatePayment(OrderOutboxEvent event) {
        Order order = findOrder(event.getOrderNo());
        Map<String, String> payload = OrderOutboxPayloadCodec.decode(event.getPayload());
        String channelCode = payload.getOrDefault("channelCode", "MOCK");
        PaymentCreateFacadeResponse paymentResult = BaconContextHolder.callWithTenantId(
                BaconContextHolder.requireTenantId(),
                () -> paymentCommandFacade.createPayment(new PaymentCreateFacadeRequest(
                        order.getOrderNo() == null ? null : order.getOrderNo().value(),
                        order.getUserId() == null ? null : order.getUserId().value(),
                        order.getPayableAmount().value(),
                        channelCode,
                        "order:" + (order.getOrderNo() == null ? null : order.getOrderNo().value()),
                        order.getExpiredAt())));
        boolean created = order.recordPaymentCreationResult(
                toPaymentNo(paymentResult.getPaymentNo()),
                toPayStatus(paymentResult.getPaymentStatus()),
                paymentResult.getChannelCode(),
                CLOSE_REASON_PAYMENT_CREATE_FAILED);
        orderRepository.update(order);
        if (!created) {
            orderDerivedDataPersistenceSupport.persist(
                    order, OrderAuditActionType.OUTBOX_CREATE_PAYMENT_FAILED, OrderStatus.RESERVING_STOCK);

            Map<String, String> releasePayload = new LinkedHashMap<>();
            releasePayload.put("reason", CLOSE_REASON_PAYMENT_CREATE_FAILED);
            orderOutboxRepository.insert(OrderOutboxEvent.create(
                    order.getOrderNo() == null ? null : order.getOrderNo().value(),
                    OrderOutboxEventType.RELEASE_STOCK,
                    (order.getOrderNo() == null ? null : order.getOrderNo().value()) + ":RELEASE_PAYMENT_CREATE_FAILED",
                    OrderOutboxPayloadCodec.encode(releasePayload),
                    OrderOutboxStatus.NEW,
                    0,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Instant.now(),
                    Instant.now()));
            return;
        }
        orderDerivedDataPersistenceSupport.persist(
                order, OrderAuditActionType.OUTBOX_CREATE_PAYMENT_OK, OrderStatus.RESERVING_STOCK);
    }

    private void executeReleaseStock(OrderOutboxEvent event) {
        Order order = findOrder(event.getOrderNo());
        String reason = OrderOutboxPayloadCodec.decode(event.getPayload()).getOrDefault("reason", "SYSTEM_CANCELLED");
        InventoryReservationFacadeResponse releaseResult = BaconContextHolder.callWithTenantId(
                BaconContextHolder.requireTenantId(),
                () -> inventoryCommandFacade.releaseReservedStock(new InventoryReleaseFacadeRequest(
                        order.getOrderNo() == null ? null : order.getOrderNo().value(), reason)));
        order.recordInventoryReleaseResult(
                toInventoryStatus(releaseResult.getInventoryStatus()),
                ReservationNoCodec.toDomain(releaseResult.getReservationNo()),
                toWarehouseCode(releaseResult.getWarehouseCode()),
                releaseResult.getReleaseReason(),
                releaseResult.getReleasedAt(),
                resolveReason(releaseResult.getFailureReason(), reason));
        orderRepository.update(order);
        orderDerivedDataPersistenceSupport.persist(order, OrderAuditActionType.OUTBOX_RELEASE, order.getOrderStatus());
    }

    private Order findOrder(OrderNo orderNo) {
        return orderRepository
                .findByOrderNo(orderNo)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderNo));
    }

    private InventoryStatus toInventoryStatus(String inventoryStatus) {
        return inventoryStatus == null || inventoryStatus.isBlank() ? null : InventoryStatus.from(inventoryStatus);
    }

    private PayStatus toPayStatus(String payStatus) {
        return payStatus == null || payStatus.isBlank() ? null : PayStatus.from(payStatus);
    }

    private PaymentNo toPaymentNo(String paymentNo) {
        return paymentNo == null || paymentNo.isBlank() ? null : PaymentNo.of(paymentNo);
    }

    private WarehouseCode toWarehouseCode(String warehouseCode) {
        return warehouseCode == null || warehouseCode.isBlank() ? null : WarehouseCode.of(warehouseCode);
    }

    private String resolveReason(String reason, String fallbackReason) {
        return reason == null || reason.isBlank() ? fallbackReason : reason;
    }
}
