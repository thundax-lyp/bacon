package com.github.thundax.bacon.order.application.saga;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxRepository;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
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

    public OrderOutboxActionExecutor(OrderRepository orderRepository,
                                     OrderOutboxRepository orderOutboxRepository,
                                     InventoryCommandFacade inventoryCommandFacade,
                                     PaymentCommandFacade paymentCommandFacade) {
        this.orderRepository = orderRepository;
        this.orderOutboxRepository = orderOutboxRepository;
        this.inventoryCommandFacade = inventoryCommandFacade;
        this.paymentCommandFacade = paymentCommandFacade;
    }

    public void enqueueReserveStock(Long tenantId, String orderNo, String channelCode) {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("channelCode", channelCode == null ? "MOCK" : channelCode);
        orderOutboxRepository.saveOutboxEvent(new OrderOutboxEvent(null, tenantId, orderNo,
                OrderOutboxEvent.EVENT_RESERVE_STOCK,
                tenantId + ":" + orderNo + ":RESERVE", OrderOutboxPayloadCodec.encode(payload),
                OrderOutboxEvent.STATUS_NEW, 0, null, null, null, null, null,
                null, Instant.now(), Instant.now()));
    }

    public void executeClaimed(OrderOutboxEvent event) {
        if (OrderOutboxEvent.EVENT_RESERVE_STOCK.equals(event.getEventType())) {
            executeReserveStock(event);
            return;
        }
        if (OrderOutboxEvent.EVENT_CREATE_PAYMENT.equals(event.getEventType())) {
            executeCreatePayment(event);
            return;
        }
        if (OrderOutboxEvent.EVENT_RELEASE_STOCK.equals(event.getEventType())) {
            executeReleaseStock(event);
            return;
        }
        throw new IllegalStateException("Unsupported outbox event type: " + event.getEventType());
    }

    private void executeReserveStock(OrderOutboxEvent event) {
        Order order = findOrder(event.getTenantId(), event.getOrderNo());
        List<OrderItem> items = orderRepository.findItemsByOrderId(order.getTenantId(), order.getId());
        List<InventoryReservationItemDTO> reserveItems = items.stream()
                .map(item -> new InventoryReservationItemDTO(item.getSkuId(), item.getQuantity()))
                .toList();
        InventoryReservationResultDTO reserveResult = inventoryCommandFacade.reserveStock(order.getTenantId(),
                order.getOrderNo(), reserveItems);
        if (!Order.INVENTORY_STATUS_RESERVED.equals(reserveResult.getInventoryStatus())) {
            String reason = resolveFailureReason(reserveResult.getFailureReason(), "inventory reserve failed");
            order.markInventoryFailed(reserveResult.getReservationNo(), reserveResult.getWarehouseId(), reason);
            order.closeByInventoryReserveFailed(CLOSE_REASON_INVENTORY_RESERVE_FAILED);
            orderRepository.save(order);
            persistDerivedData(order, "OUTBOX_RESERVE_FAILED", Order.ORDER_STATUS_RESERVING_STOCK);
            return;
        }
        order.markInventoryReserved(reserveResult.getReservationNo(), reserveResult.getWarehouseId());
        orderRepository.save(order);
        persistDerivedData(order, "OUTBOX_RESERVE_OK", Order.ORDER_STATUS_RESERVING_STOCK);

        Map<String, String> source = OrderOutboxPayloadCodec.decode(event.getPayload());
        String channelCode = source.getOrDefault("channelCode", "MOCK");
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("channelCode", channelCode);
        orderOutboxRepository.saveOutboxEvent(new OrderOutboxEvent(null, order.getTenantId(), order.getOrderNo(),
                OrderOutboxEvent.EVENT_CREATE_PAYMENT,
                order.getTenantId() + ":" + order.getOrderNo() + ":CREATE_PAYMENT",
                OrderOutboxPayloadCodec.encode(payload), OrderOutboxEvent.STATUS_NEW, 0, null, null, null,
                null, null, null, Instant.now(), Instant.now()));
    }

    private void executeCreatePayment(OrderOutboxEvent event) {
        Order order = findOrder(event.getTenantId(), event.getOrderNo());
        Map<String, String> payload = OrderOutboxPayloadCodec.decode(event.getPayload());
        String channelCode = payload.getOrDefault("channelCode", "MOCK");
        PaymentCreateResultDTO paymentResult = paymentCommandFacade.createPayment(order.getTenantId(), order.getOrderNo(),
                order.getUserId(), order.getPayableAmount(), channelCode, "order:" + order.getOrderNo(),
                order.getExpiredAt());
        if (paymentResult.getPaymentNo() == null || paymentResult.getPaymentNo().isBlank()
                || !Order.PAY_STATUS_PAYING.equals(paymentResult.getPaymentStatus())) {
            order.closeByPaymentCreateFailed(CLOSE_REASON_PAYMENT_CREATE_FAILED);
            orderRepository.save(order);
            persistDerivedData(order, "OUTBOX_CREATE_PAYMENT_FAILED", Order.ORDER_STATUS_RESERVING_STOCK);

            Map<String, String> releasePayload = new LinkedHashMap<>();
            releasePayload.put("reason", CLOSE_REASON_PAYMENT_CREATE_FAILED);
            orderOutboxRepository.saveOutboxEvent(new OrderOutboxEvent(null, order.getTenantId(), order.getOrderNo(),
                    OrderOutboxEvent.EVENT_RELEASE_STOCK,
                    order.getTenantId() + ":" + order.getOrderNo() + ":RELEASE_PAYMENT_CREATE_FAILED",
                    OrderOutboxPayloadCodec.encode(releasePayload), OrderOutboxEvent.STATUS_NEW, 0, null,
                    null, null, null, null, null, Instant.now(), Instant.now()));
            return;
        }
        order.markPendingPayment(paymentResult.getPaymentNo(), paymentResult.getChannelCode());
        orderRepository.save(order);
        persistDerivedData(order, "OUTBOX_CREATE_PAYMENT_OK", Order.ORDER_STATUS_RESERVING_STOCK);
    }

    private void executeReleaseStock(OrderOutboxEvent event) {
        Order order = findOrder(event.getTenantId(), event.getOrderNo());
        String reason = OrderOutboxPayloadCodec.decode(event.getPayload()).getOrDefault("reason", "SYSTEM_CANCELLED");
        InventoryReservationResultDTO releaseResult = inventoryCommandFacade.releaseReservedStock(order.getTenantId(),
                order.getOrderNo(), reason);
        if (Order.INVENTORY_STATUS_RELEASED.equals(releaseResult.getInventoryStatus())) {
            order.markInventoryReleased(releaseResult.getReservationNo(), releaseResult.getWarehouseId(),
                    releaseResult.getReleaseReason(), releaseResult.getReleasedAt());
        } else {
            order.markInventoryFailed(releaseResult.getReservationNo(), releaseResult.getWarehouseId(),
                    resolveFailureReason(releaseResult.getFailureReason(), reason));
        }
        orderRepository.save(order);
        persistDerivedData(order, "OUTBOX_RELEASE", order.getOrderStatus());
    }

    private Order findOrder(Long tenantId, String orderNo) {
        return orderRepository.findByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
    }

    private String resolveFailureReason(String reason, String defaultReason) {
        return reason == null || reason.isBlank() ? defaultReason : reason;
    }

    private void persistDerivedData(Order order, String actionType, String beforeStatus) {
        Instant now = Instant.now();
        if (order.getPaymentNo() != null && !order.getPaymentNo().isBlank()) {
            orderRepository.savePaymentSnapshot(new OrderPaymentSnapshot(null, order.getTenantId(), order.getId(),
                    order.getPaymentNo(), order.getPaymentChannelCode(), order.getPayStatus(), order.getPaidAmount(),
                    order.getPaidAt(), order.getPaymentFailureReason(), order.getPaymentChannelStatus(), now));
        }
        if (order.getReservationNo() != null && !order.getReservationNo().isBlank()) {
            orderRepository.saveInventorySnapshot(new OrderInventorySnapshot(null, order.getTenantId(), order.getId(),
                    order.getReservationNo(), order.getInventoryStatus(), order.getWarehouseId(),
                    order.getInventoryFailureReason(), now));
        }
        orderRepository.saveAuditLog(new OrderAuditLog(null, order.getTenantId(), order.getOrderNo(), actionType,
                beforeStatus, order.getOrderStatus(), "SYSTEM", 0L, now));
    }
}
