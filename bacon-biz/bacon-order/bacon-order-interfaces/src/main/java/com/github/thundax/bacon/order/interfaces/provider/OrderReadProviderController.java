package com.github.thundax.bacon.order.interfaces.provider;

import com.github.thundax.bacon.order.interfaces.assembler.OrderInterfaceAssembler;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.api.request.OrderPageFacadeRequest;
import com.github.thundax.bacon.order.api.response.OrderDetailFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderPageFacadeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/providers/order")
@Tag(name = "Inner-Order-Management", description = "Order 域内部 Provider 接口")
public class OrderReadProviderController {

    private final OrderQueryApplicationService orderQueryService;

    public OrderReadProviderController(OrderQueryApplicationService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @Operation(summary = "按订单号查询订单详情")
    @GetMapping("/{orderNo}")
    public OrderDetailFacadeResponse getByOrderNo(@PathVariable("orderNo") @NotBlank String orderNo) {
        return OrderInterfaceAssembler.toDetailFacadeResponse(
                orderQueryService.getByOrderNo(OrderInterfaceAssembler.toByOrderNoQuery(orderNo)));
    }

    @Operation(summary = "分页查询订单")
    @GetMapping
    public OrderPageFacadeResponse page(@Valid OrderPageFacadeRequest request) {
        return OrderInterfaceAssembler.toPageFacadeResponse(
                orderQueryService.page(OrderInterfaceAssembler.toPageQuery(request)));
    }
}
