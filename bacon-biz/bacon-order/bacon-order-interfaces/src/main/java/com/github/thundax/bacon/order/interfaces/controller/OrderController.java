package com.github.thundax.bacon.order.interfaces.controller;

import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.application.service.OrderApplicationService;
import com.github.thundax.bacon.order.application.service.OrderCancelApplicationService;
import com.github.thundax.bacon.order.application.service.OrderQueryApplicationService;
import com.github.thundax.bacon.order.interfaces.dto.CreateOrderRequest;
import com.github.thundax.bacon.order.interfaces.response.OrderDetailResponse;
import com.github.thundax.bacon.order.interfaces.response.OrderPageResponse;
import com.github.thundax.bacon.order.interfaces.response.OrderSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@WrappedApiController
@RequestMapping("/api/orders")
@Tag(name = "Order-Management", description = "订单创建、查询与取消接口")
public class OrderController {

    private final OrderApplicationService orderApplicationService;
    private final OrderQueryApplicationService orderQueryService;
    private final OrderCancelApplicationService orderCancelApplicationService;

    public OrderController(OrderApplicationService orderApplicationService, OrderQueryApplicationService orderQueryService,
                           OrderCancelApplicationService orderCancelApplicationService) {
        this.orderApplicationService = orderApplicationService;
        this.orderQueryService = orderQueryService;
        this.orderCancelApplicationService = orderCancelApplicationService;
    }

    @Operation(summary = "创建订单")
    @HasPermission("order:order:create")
    @PostMapping
    public OrderSummaryResponse create(@RequestBody CreateOrderRequest request) {
        return OrderSummaryResponse.from(orderApplicationService.create(request.toCommand()));
    }

    @Operation(summary = "按 ID 查询订单")
    @HasPermission("order:order:view")
    @GetMapping("/{orderId}")
    public OrderDetailResponse getById(@RequestParam(value = "tenantId", defaultValue = "1001") Long tenantId,
                                       @PathVariable Long orderId) {
        return OrderDetailResponse.from(orderQueryService.getById(tenantId, orderId));
    }

    @Operation(summary = "分页查询订单")
    @HasPermission("order:order:view")
    @GetMapping
    public OrderPageResponse pageOrders(@RequestParam(value = "tenantId", defaultValue = "1001") Long tenantId,
                                        @RequestParam(value = "userId", required = false) Long userId,
                                        @RequestParam(value = "orderNo", required = false) String orderNo) {
        return OrderPageResponse.from(orderQueryService.pageOrders(new OrderPageQueryDTO(tenantId, userId, orderNo,
                null, null, null, (Instant) null, (Instant) null, 1, 20)));
    }

    @Operation(summary = "取消订单")
    @HasPermission("order:order:cancel")
    @PostMapping("/{orderNo}/cancel")
    public void cancel(@RequestParam(value = "tenantId", defaultValue = "1001") Long tenantId,
                       @PathVariable String orderNo) {
        orderCancelApplicationService.cancel(tenantId, orderNo);
    }
}
