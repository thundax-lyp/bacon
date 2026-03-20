package com.github.thundax.bacon.order.interfaces.controller;

import com.github.thundax.bacon.order.application.query.GetOrderQuery;
import com.github.thundax.bacon.order.application.service.OrderApplicationService;
import com.github.thundax.bacon.order.interfaces.assembler.OrderAssembler;
import com.github.thundax.bacon.order.interfaces.dto.CreateOrderRequest;
import com.github.thundax.bacon.order.interfaces.vo.OrderVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    public OrderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @PostMapping
    public OrderVO create(@RequestBody CreateOrderRequest request) {
        return OrderAssembler.toVO(orderApplicationService.create(OrderAssembler.toCommand(request)));
    }

    @GetMapping("/{orderId}")
    public OrderVO getById(@PathVariable Long orderId) {
        return OrderAssembler.toVO(orderApplicationService.get(new GetOrderQuery(orderId)));
    }
}
