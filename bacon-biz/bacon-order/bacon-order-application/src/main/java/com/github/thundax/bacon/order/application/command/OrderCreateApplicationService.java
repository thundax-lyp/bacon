package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.mapper.UserIdMapper;
import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.codec.OrderIdCodec;
import com.github.thundax.bacon.order.application.codec.ReservationNoCodec;
import com.github.thundax.bacon.order.application.saga.OrderOutboxActionExecutor;
import com.github.thundax.bacon.order.application.support.OrderDerivedDataPersistenceSupport;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.enums.OrderAuditActionType;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.order.domain.service.OrderNoGenerator;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderCreateApplicationService {

    private static final OrderAuditActionType ACTION_CREATE = OrderAuditActionType.ORDER_CREATE;

    private final OrderRepository orderRepository;
    private final OrderNoGenerator orderNoGenerator;
    private final OrderOutboxActionExecutor orderOutboxActionExecutor;
    private final OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport;

    public OrderCreateApplicationService(
            OrderRepository orderRepository,
            OrderNoGenerator orderNoGenerator,
            OrderOutboxActionExecutor orderOutboxActionExecutor,
            OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport) {
        this.orderRepository = orderRepository;
        this.orderNoGenerator = orderNoGenerator;
        this.orderOutboxActionExecutor = orderOutboxActionExecutor;
        this.orderDerivedDataPersistenceSupport = orderDerivedDataPersistenceSupport;
    }

    public OrderSummaryDTO create(CreateOrderCommand command) {
        if (command.userId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        List<CreateOrderItemCommand> items = command.items() == null ? List.of() : command.items();
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        BaconContextHolder.requireTenantId();
        BigDecimal totalAmount = items.stream().map(this::calculateLineAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        OrderNo orderNo = orderNoGenerator.nextOrderNo();
        CurrencyCode currencyCode = resolveCurrencyCode(command.currencyCode());
        Order order = Order.create(
                null,
                orderNo.value(),
                command.userId(),
                currencyCode,
                totalAmount.toPlainString(),
                totalAmount.toPlainString(),
                command.remark(),
                command.expiredAt());
        Order savedOrder = orderRepository.save(order);
        orderRepository.saveItems(
                valueOf(savedOrder.getId()),
                items.stream()
                        .map(item -> OrderItem.create(
                                valueOf(savedOrder.getId()),
                                item.skuId(),
                                item.skuName(),
                                item.imageUrl(),
                                item.quantity(),
                                currencyCode,
                                item.salePrice().toPlainString(),
                                calculateLineAmount(item).toPlainString()))
                        .toList());
        savedOrder.markReservingStock();
        orderRepository.save(savedOrder);
        orderOutboxActionExecutor.enqueueReserveStock(valueOf(savedOrder.getOrderNo()), command.channelCode());
        orderDerivedDataPersistenceSupport.persist(savedOrder, ACTION_CREATE, OrderStatus.CREATED);
        return new OrderSummaryDTO(
                valueOf(savedOrder.getId()),
                BaconContextHolder.requireTenantId(),
                valueOf(savedOrder.getOrderNo()),
                savedOrder.getUserId() == null ? null : savedOrder.getUserId().value(),
                valueOf(savedOrder.getOrderStatus()),
                valueOf(savedOrder.getPayStatus()),
                valueOf(savedOrder.getInventoryStatus()),
                valueOf(savedOrder.getPaymentNo()),
                valueOf(savedOrder.getReservationNo()),
                valueOf(savedOrder.getCurrencyCode()),
                savedOrder.getTotalAmount().value(),
                savedOrder.getPayableAmount().value(),
                savedOrder.getCancelReason(),
                savedOrder.getCloseReason(),
                savedOrder.getCreatedAt(),
                savedOrder.getExpiredAt());
    }

    private BigDecimal calculateLineAmount(CreateOrderItemCommand item) {
        if (item == null || item.quantity() == null || item.salePrice() == null) {
            throw new IllegalArgumentException("order item quantity and salePrice are required");
        }
        return item.salePrice().multiply(BigDecimal.valueOf(item.quantity()));
    }

    private CurrencyCode resolveCurrencyCode(String currencyCode) {
        return currencyCode == null || currencyCode.isBlank() ? CurrencyCode.RMB : CurrencyCode.fromValue(currencyCode);
    }

    private Long valueOf(OrderId orderId) {
        return OrderIdCodec.toValue(orderId);
    }

    private String valueOf(OrderNo orderNo) {
        return orderNo == null ? null : orderNo.value();
    }

    private String valueOf(com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo reservationNo) {
        return ReservationNoCodec.toValue(reservationNo);
    }

    private String valueOf(com.github.thundax.bacon.common.commerce.valueobject.PaymentNo paymentNo) {
        return paymentNo == null ? null : paymentNo.value();
    }

    private String valueOf(CurrencyCode currencyCode) {
        return currencyCode == null ? null : currencyCode.value();
    }

    private String valueOf(com.github.thundax.bacon.order.domain.model.enums.OrderStatus orderStatus) {
        return orderStatus == null ? null : orderStatus.value();
    }

    private String valueOf(com.github.thundax.bacon.order.domain.model.enums.PayStatus payStatus) {
        return payStatus == null ? null : payStatus.value();
    }

    private String valueOf(com.github.thundax.bacon.order.domain.model.enums.InventoryStatus inventoryStatus) {
        return inventoryStatus == null ? null : inventoryStatus.value();
    }
}
