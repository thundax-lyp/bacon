package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.common.core.enums.CurrencyCode;
import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.mapper.TenantIdMapper;
import com.github.thundax.bacon.common.id.mapper.UserIdMapper;
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
        Order order = orderFactory.create(null, TenantIdMapper.toDomain(command.tenantId()), orderNo, UserIdMapper.toDomain(command.userId()), currencyCode,
                Money.of(totalAmount, currencyCode), Money.of(totalAmount, currencyCode), command.remark(),
                command.expiredAt());
        Order savedOrder = orderRepository.save(order);
        orderRepository.saveItems(savedOrder.getTenantIdValue(), savedOrder.getIdValue(), items.stream()
                .map(item -> new OrderItem(savedOrder.getTenantIdValue(), savedOrder.getIdValue(), item.skuId(),
                        item.skuName(), item.imageUrl(), item.quantity(), currencyCode,
                        item.salePrice().toPlainString(), calculateLineAmount(item).toPlainString()))
                .toList());
        savedOrder.markReservingStock();
        orderRepository.save(savedOrder);
        orderOutboxActionExecutor.enqueueReserveStock(savedOrder.getTenantIdValue(), savedOrder.getOrderNoValue(),
                command.channelCode());
        orderDerivedDataPersistenceSupport.persist(savedOrder, ACTION_CREATE, OrderStatus.CREATED);
        return new OrderSummaryDTO(savedOrder.getIdValue(), savedOrder.getTenantIdValue(),
                savedOrder.getOrderNoValue(), savedOrder.getUserIdValue(),
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

    private OrderId toOrderId(Order order) {
        return order.getId();
    }

    private SkuId toSkuId(Long skuId) {
        return skuId == null ? null : SkuId.of(skuId);
    }
}
