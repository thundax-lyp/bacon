package com.github.thundax.bacon.order.interfaces.controller;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.order.application.codec.OrderIdCodec;
import com.github.thundax.bacon.order.application.command.OrderCancelApplicationService;
import com.github.thundax.bacon.order.application.command.OrderCreateApplicationService;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.interfaces.dto.CancelOrderRequest;
import com.github.thundax.bacon.order.interfaces.dto.CreateOrderRequest;
import com.github.thundax.bacon.order.interfaces.dto.OrderPageRequest;
import com.github.thundax.bacon.order.interfaces.response.OrderDetailResponse;
import com.github.thundax.bacon.order.interfaces.response.OrderPageResponse;
import com.github.thundax.bacon.order.interfaces.response.OrderSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
@RequestMapping("/order")
@Tag(name = "Order-Management", description = "订单创建、查询与取消接口")
public class OrderController {

    private final OrderCreateApplicationService orderCreateApplicationService;
    private final OrderQueryApplicationService orderQueryService;
    private final OrderCancelApplicationService orderCancelApplicationService;

    public OrderController(
            OrderCreateApplicationService orderCreateApplicationService,
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
    public OrderDetailResponse getById(@PathVariable Long orderId) {
        return OrderDetailResponse.from(orderQueryService.getById(OrderIdCodec.toDomain(orderId)));
    }

    @Operation(summary = "分页查询订单")
    @HasPermission("order:order:view")
    @GetMapping
    public OrderPageResponse pageOrders(@Valid @ModelAttribute OrderPageRequest request) {
        return OrderPageResponse.from(orderQueryService.pageOrders(
                UserIdCodec.toDomain(request.getUserId()),
                OrderNoCodec.toDomain(request.getOrderNo()),
                request.getOrderStatus() == null ? null : OrderStatus.from(request.getOrderStatus()),
                request.getPayStatus() == null ? null : PayStatus.from(request.getPayStatus()),
                request.getInventoryStatus() == null ? null : InventoryStatus.from(request.getInventoryStatus()),
                request.getCreatedAtFrom(),
                request.getCreatedAtTo(),
                request.getPageNo(),
                request.getPageSize()));
    }

    @Operation(summary = "取消订单")
    @HasPermission("order:order:cancel")
    @PostMapping("/{orderNo}/cancel")
    public void cancel(@PathVariable String orderNo, @RequestBody CancelOrderRequest request) {
        orderCancelApplicationService.cancel(OrderNoCodec.toDomain(orderNo), request.reason());
    }
}
