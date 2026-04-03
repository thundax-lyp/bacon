package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.core.enums.CurrencyCode;
import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.saga.OrderOutboxActionExecutor;
import com.github.thundax.bacon.order.application.support.OrderDerivedDataPersistenceSupport;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.order.domain.service.OrderDomainService;
import com.github.thundax.bacon.order.domain.service.OrderNoGenerator;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderCreateApplicationService {

    private static final String ACTION_CREATE = "ORDER_CREATE";

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService = new OrderDomainService();
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
        String orderNo = orderNoGenerator.nextOrderNo();
        CurrencyCode currencyCode = resolveCurrencyCode(command.currencyCode());
        Order order = orderDomainService.create(null, command.tenantId(), orderNo, command.userId(),
                Money.of(totalAmount, currencyCode), Money.of(totalAmount, currencyCode), command.remark(),
                command.expiredAt());
        Order savedOrder = orderRepository.save(order);
        orderRepository.saveItems(savedOrder.getTenantId(), savedOrder.getId(), items.stream()
                .map(item -> new OrderItem(savedOrder.getTenantId(), savedOrder.getId(), item.skuId(), item.skuName(),
                        item.quantity(), item.salePrice(), calculateLineAmount(item)))
                .toList());
        savedOrder.markReservingStock();
        orderRepository.save(savedOrder);
        orderOutboxActionExecutor.enqueueReserveStock(savedOrder.getTenantId(), savedOrder.getOrderNo(),
                command.channelCode());
        orderDerivedDataPersistenceSupport.persist(savedOrder, ACTION_CREATE, Order.ORDER_STATUS_CREATED);
        return new OrderSummaryDTO(savedOrder.getId(), savedOrder.getTenantId(), savedOrder.getOrderNo(),
                savedOrder.getUserId(), savedOrder.getOrderStatus(), savedOrder.getPayStatus(),
                savedOrder.getInventoryStatus(), savedOrder.getPaymentNo(), savedOrder.getReservationNo(),
                savedOrder.getCurrencyCode(), savedOrder.getTotalAmount().value(), savedOrder.getPayableAmount().value(),
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
}
