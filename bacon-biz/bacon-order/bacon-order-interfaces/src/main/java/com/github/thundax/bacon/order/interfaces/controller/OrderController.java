package com.github.thundax.bacon.order.interfaces.controller;

import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.application.command.OrderCreateApplicationService;
import com.github.thundax.bacon.order.application.command.OrderCancelApplicationService;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.interfaces.dto.CancelOrderRequest;
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

    private final OrderCreateApplicationService orderCreateApplicationService;
    private final OrderQueryApplicationService orderQueryService;
    private final OrderCancelApplicationService orderCancelApplicationService;

    public OrderController(OrderCreateApplicationService orderCreateApplicationService,
                           OrderQueryApplicationService orderQueryService,
                           OrderCancelApplicationService orderCancelApplicationService) {
        this.orderCreateApplicationService = orderCreateApplicationService;
        this.orderQueryService = orderQueryService;
        this.orderCancelApplicationService = orderCancelApplicationService;
    }

    @Operation(summary = "创建订单")
    @HasPermission("order:order:create")
    @PostMapping
    public OrderSummaryResponse create(@RequestBody CreateOrderRequest request) {
        return OrderSummaryResponse.from(orderCreateApplicationService.create(request.toCommand()));
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
                                        @RequestParam(value = "orderNo", required = false) String orderNo,
                                        @RequestParam(value = "orderStatus", required = false) String orderStatus,
                                        @RequestParam(value = "payStatus", required = false) String payStatus,
                                        @RequestParam(value = "inventoryStatus", required = false) String inventoryStatus,
                                        @RequestParam(value = "createdAtFrom", required = false) Instant createdAtFrom,
                                        @RequestParam(value = "createdAtTo", required = false) Instant createdAtTo,
                                        @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                        @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return OrderPageResponse.from(orderQueryService.pageOrders(new OrderPageQueryDTO(String.valueOf(tenantId),
                userId == null ? null : String.valueOf(userId), orderNo,
                orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo, pageNo, pageSize)));
    }
    @Operation(summary = "取消订单")
    @HasPermission("order:order:cancel")
    @PostMapping("/{orderNo}/cancel")
    public void cancel(@PathVariable String orderNo, @RequestBody CancelOrderRequest request) {
        orderCancelApplicationService.cancel(request.tenantId(), orderNo, request.reason());
    }
}
