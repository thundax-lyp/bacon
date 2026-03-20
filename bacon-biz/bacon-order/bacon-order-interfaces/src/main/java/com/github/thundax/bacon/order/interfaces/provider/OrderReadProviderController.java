package com.github.thundax.bacon.order.interfaces.provider;

import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.query.GetOrderQuery;
import com.github.thundax.bacon.order.application.service.OrderApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/providers/orders")
public class OrderReadProviderController {

    private final OrderApplicationService orderApplicationService;

    public OrderReadProviderController(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @GetMapping("/{orderId}")
    public OrderSummaryDTO getById(@PathVariable Long orderId) {
        return orderApplicationService.get(new GetOrderQuery(orderId));
    }
}
