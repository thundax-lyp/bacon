package com.github.thundax.bacon.order.interfaces.controller;

import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.service.OrderApplicationService;
import com.github.thundax.bacon.order.application.service.OrderCancelApplicationService;
import com.github.thundax.bacon.order.application.service.OrderQueryService;
import com.github.thundax.bacon.order.interfaces.assembler.OrderAssembler;
import com.github.thundax.bacon.order.interfaces.dto.CreateOrderRequest;
import com.github.thundax.bacon.order.interfaces.vo.OrderVO;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderApplicationService orderApplicationService;
    private final OrderQueryService orderQueryService;
    private final OrderCancelApplicationService orderCancelApplicationService;

    public OrderController(OrderApplicationService orderApplicationService, OrderQueryService orderQueryService,
                           OrderCancelApplicationService orderCancelApplicationService) {
        this.orderApplicationService = orderApplicationService;
        this.orderQueryService = orderQueryService;
        this.orderCancelApplicationService = orderCancelApplicationService;
    }

    @PostMapping
    public OrderVO create(@RequestBody CreateOrderRequest request) {
        return OrderAssembler.toVO(orderApplicationService.create(OrderAssembler.toCommand(request)));
    }

    @GetMapping("/{orderId}")
    public OrderDetailDTO getById(@RequestParam(value = "tenantId", defaultValue = "1001") Long tenantId,
                                  @PathVariable Long orderId) {
        return orderQueryService.getById(tenantId, orderId);
    }

    @GetMapping
    public OrderPageResultDTO pageOrders(@RequestParam(value = "tenantId", defaultValue = "1001") Long tenantId,
                                         @RequestParam(value = "userId", required = false) Long userId,
                                         @RequestParam(value = "orderNo", required = false) String orderNo) {
        return orderQueryService.pageOrders(new OrderPageQueryDTO(tenantId, userId, orderNo, null, null, null,
                (Instant) null, (Instant) null, 1, 20));
    }

    @PostMapping("/{orderNo}/cancel")
    public void cancel(@RequestParam(value = "tenantId", defaultValue = "1001") Long tenantId, @PathVariable String orderNo) {
        orderCancelApplicationService.cancel(tenantId, orderNo);
    }
}
