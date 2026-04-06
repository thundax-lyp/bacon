package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.core.enums.CurrencyCode;
import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.saga.OrderOutboxActionExecutor;
import com.github.thundax.bacon.order.application.support.OrderDerivedDataPersistenceSupport;
import com.github.thundax.bacon.order.domain.factory.OrderFactory;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.enums.OrderAuditActionType;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.order.domain.service.OrderNoGenerator;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderNo;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderCreateApplicationService {

    private static final OrderAuditActionType ACTION_CREATE = OrderAuditActionType.ORDER_CREATE;

    private final OrderRepository orderRepository;
    private final OrderFactory orderFactory = new OrderFactory();
    private final OrderNoGenerator orderNoGenerator;
    private final OrderOutboxActionExecutor orderOutboxActionExecutor;
    private final OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport;

    public OrderCreateApplicationService(OrderRepository orderRepository, OrderNoGenerator orderNoGenerator,
                                         OrderOutboxActionExecutor orderOutboxActionExecutor,
                                         OrderDerivedDataPersistenceSupport orderDerivedDataPersistenceSupport) {
        this.orderRepository = orderRepository;
        this.orderNoGenerator = orderNoGenerator;
        this.orderOutboxActionExecutor = orderOutboxActionExecutor;
        this.orderDerivedDataPersistenceSupport = orderDerivedDataPersistenceSupport;
    }

    public OrderSummaryDTO create(CreateOrderCommand command) {
        if (command.tenantId() == null || command.userId() == null) {
            throw new IllegalArgumentException("tenantId and userId are required");
        }
        List<CreateOrderItemCommand> items = command.items() == null ? List.of() : command.items();
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        BigDecimal totalAmount = items.stream()
                .map(this::calculateLineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        OrderNo orderNo = orderNoGenerator.nextOrderNo();
        CurrencyCode currencyCode = resolveCurrencyCode(command.currencyCode());
        Order order = orderFactory.create(null, toTenantId(command.tenantId()), orderNo, toUserId(command.userId()), currencyCode,
                Money.of(totalAmount, currencyCode), Money.of(totalAmount, currencyCode), command.remark(),
                command.expiredAt());
        Order savedOrder = orderRepository.save(order);
        orderRepository.saveItems(toTenantIdValue(savedOrder), toOrderIdValue(savedOrder), items.stream()
                .map(item -> new OrderItem(savedOrder.getTenantId(), toOrderId(savedOrder), toSkuId(item.skuId()),
                        item.skuName(), item.imageUrl(), item.quantity(), Money.of(item.salePrice(), currencyCode),
                        Money.of(calculateLineAmount(item), currencyCode)))
                .toList());
        savedOrder.markReservingStock();
        orderRepository.save(savedOrder);
        orderOutboxActionExecutor.enqueueReserveStock(toTenantIdValue(savedOrder), savedOrder.getOrderNoValue(),
                command.channelCode());
        orderDerivedDataPersistenceSupport.persist(savedOrder, ACTION_CREATE, OrderStatus.CREATED);
        return new OrderSummaryDTO(toOrderIdValue(savedOrder), toTenantIdValue(savedOrder),
                savedOrder.getOrderNoValue(), toUserIdValue(savedOrder),
                savedOrder.getOrderStatusValue(), savedOrder.getPayStatusValue(),
                savedOrder.getInventoryStatusValue(), savedOrder.getPaymentNoValue(), savedOrder.getReservationNoValue(),
                savedOrder.getCurrencyCodeValue(), savedOrder.getTotalAmount().value(), savedOrder.getPayableAmount().value(),
                savedOrder.getCancelReason(), savedOrder.getCloseReason(), savedOrder.getCreatedAt(),
                savedOrder.getExpiredAt());
    }

    private BigDecimal calculateLineAmount(CreateOrderItemCommand item) {
        if (item == null || item.quantity() == null || item.salePrice() == null) {
            throw new IllegalArgumentException("order item quantity and salePrice are required");
        }
        return item.salePrice().multiply(BigDecimal.valueOf(item.quantity()));
    }

    private CurrencyCode resolveCurrencyCode(String currencyCode) {
        return currencyCode == null || currencyCode.isBlank()
                ? CurrencyCode.RMB
                : CurrencyCode.fromValue(currencyCode);
    }

    private Long toOrderIdValue(Order order) {
        return order.getId() == null ? null : order.getId().value();
    }

    private Long toTenantIdValue(Order order) {
        return order.getTenantId() == null ? null : order.getTenantId().value();
    }

    private OrderId toOrderId(Order order) {
        return order.getId();
    }

    private SkuId toSkuId(Long skuId) {
        return skuId == null ? null : SkuId.of(skuId);
    }

    private UserId toUserId(Long userId) {
        return userId == null ? null : UserId.of(String.valueOf(userId));
    }

    private TenantId toTenantId(Long tenantId) {
        return tenantId == null ? null : TenantId.of(tenantId);
    }

    private Long toUserIdValue(Order order) {
        return order.getUserId() == null ? null : Long.valueOf(order.getUserId().value());
    }
}
