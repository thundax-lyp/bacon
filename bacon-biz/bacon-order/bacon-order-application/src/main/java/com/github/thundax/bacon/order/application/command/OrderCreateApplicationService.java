package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.commerce.codec.SkuIdCodec;
import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.order.application.codec.ReservationNoCodec;
import com.github.thundax.bacon.order.application.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.saga.OrderOutboxActionExecutor;
import com.github.thundax.bacon.order.application.support.OrderDerivedDataPersistenceSupport;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.enums.OrderAuditActionType;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.order.domain.service.OrderNoGenerator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderCreateApplicationService {

    private static final OrderAuditActionType ACTION_CREATE = OrderAuditActionType.ORDER_CREATE;
    private static final String ORDER_ITEM_ID_BIZ_TAG = "order_item_id";

    private final OrderRepository orderRepository;
    private final OrderNoGenerator orderNoGenerator;
    private final OrderOutboxActionExecutor orderOutboxActionExecutor;
    private final OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport;
    private final IdGenerator idGenerator;

    public OrderCreateApplicationService(
            OrderRepository orderRepository,
            OrderNoGenerator orderNoGenerator,
            OrderOutboxActionExecutor orderOutboxActionExecutor,
            OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport,
            IdGenerator idGenerator) {
        this.orderRepository = orderRepository;
        this.orderNoGenerator = orderNoGenerator;
        this.orderOutboxActionExecutor = orderOutboxActionExecutor;
        this.orderDerivedDataPersistenceSupport = orderDerivedDataPersistenceSupport;
        this.idGenerator = idGenerator;
    }

    public OrderSummaryDTO create(CreateOrderCommand command) {
        if (command.userId() == null) {
            throw new BadRequestException("userId is required");
        }
        List<CreateOrderItemCommand> items = command.items() == null ? List.of() : command.items();
        if (items.isEmpty()) {
            throw new BadRequestException("items must not be empty");
        }
        BaconContextHolder.requireTenantId();
        OrderNo orderNo = orderNoGenerator.nextOrderNo();
        List<OrderItem> orderItems = items.stream()
                .map(item -> toOrderItem(item, command.currencyCode()))
                .toList();
        Order order = Order.create(
                null,
                orderNo,
                command.userId(),
                resolveCurrencyCode(command.currencyCode()),
                orderItems,
                command.remark(),
                command.expiredAt());
        Order savedOrder = orderRepository.insert(order);
        orderRepository.updateItems(
                savedOrder.getId(),
                orderItems.stream()
                        .map(item -> OrderItem.reconstruct(
                                idGenerator.nextId(ORDER_ITEM_ID_BIZ_TAG),
                                savedOrder.getId(),
                                item.getSkuId(),
                                item.getSkuName(),
                                item.getImageUrl(),
                                item.getQuantity(),
                                item.getSalePrice(),
                                item.getLineAmount()))
                        .toList());
        savedOrder.markReservingStock();
        orderRepository.update(savedOrder);
        orderOutboxActionExecutor.enqueueReserveStock(
                OrderNoCodec.toValue(savedOrder.getOrderNo()), command.channelCode());
        orderDerivedDataPersistenceSupport.persist(savedOrder, ACTION_CREATE, OrderStatus.CREATED);
        return new OrderSummaryDTO(
                savedOrder.getId() == null ? null : savedOrder.getId().value(),
                OrderNoCodec.toValue(savedOrder.getOrderNo()),
                UserIdCodec.toValue(savedOrder.getUserId()),
                savedOrder.getOrderStatus() == null
                        ? null
                        : savedOrder.getOrderStatus().value(),
                savedOrder.getPayStatus() == null
                        ? null
                        : savedOrder.getPayStatus().value(),
                savedOrder.getInventoryStatus() == null
                        ? null
                        : savedOrder.getInventoryStatus().value(),
                savedOrder.getPaymentNo() == null
                        ? null
                        : savedOrder.getPaymentNo().value(),
                savedOrder.getReservationNo() == null
                        ? null
                        : ReservationNoCodec.toValue(savedOrder.getReservationNo()),
                savedOrder.getCurrencyCode() == null
                        ? null
                        : savedOrder.getCurrencyCode().value(),
                savedOrder.getTotalAmount().value(),
                savedOrder.getPayableAmount().value(),
                savedOrder.getCancelReason(),
                savedOrder.getCloseReason(),
                savedOrder.getCreatedAt(),
                savedOrder.getExpiredAt());
    }

    private OrderItem toOrderItem(CreateOrderItemCommand item, String currencyCode) {
        if (item == null) {
            throw new BadRequestException("order item is required");
        }
        return OrderItem.create(
                null,
                null,
                SkuIdCodec.toDomain(item.skuId()),
                item.skuName(),
                item.imageUrl(),
                item.quantity(),
                Money.of(item.salePrice(), resolveCurrencyCode(currencyCode)));
    }

    private CurrencyCode resolveCurrencyCode(String currencyCode) {
        return currencyCode == null || currencyCode.isBlank() ? CurrencyCode.RMB : CurrencyCode.fromValue(currencyCode);
    }
}
