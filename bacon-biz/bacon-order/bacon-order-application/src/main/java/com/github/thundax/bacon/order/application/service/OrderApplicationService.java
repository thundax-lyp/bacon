package com.github.thundax.bacon.order.application.service;

import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.command.CreateOrderCommand;
import com.github.thundax.bacon.order.application.query.GetOrderQuery;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.order.domain.service.OrderDomainService;
import org.springframework.stereotype.Service;

@Service
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderDomainService orderDomainService = new OrderDomainService();

    public OrderApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderSummaryDTO create(CreateOrderCommand command) {
        long generatedId = System.currentTimeMillis();
        Order order = orderDomainService.create(generatedId, command.orderNo(), command.customerName());
        return toSummary(orderRepository.save(order));
    }

    public OrderSummaryDTO get(GetOrderQuery query) {
        return orderRepository.findById(query.orderId())
                .map(this::toSummary)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + query.orderId()));
    }

    private OrderSummaryDTO toSummary(Order order) {
        return new OrderSummaryDTO(order.getId(), order.getOrderNo(), order.getCustomerName());
    }
}
