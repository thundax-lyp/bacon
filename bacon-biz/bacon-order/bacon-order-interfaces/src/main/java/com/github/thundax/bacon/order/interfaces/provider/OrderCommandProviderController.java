package com.github.thundax.bacon.order.interfaces.provider;

import com.github.thundax.bacon.order.api.request.OrderMarkPaidFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaymentFailedFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderCloseExpiredFacadeRequest;
import com.github.thundax.bacon.order.application.command.OrderPaymentResultApplicationService;
import com.github.thundax.bacon.order.application.command.OrderTimeoutApplicationService;
import com.github.thundax.bacon.order.interfaces.assembler.OrderInterfaceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/providers/order")
@Tag(name = "Inner-Order-Management", description = "Order 域内部 Provider 写接口")
public class OrderCommandProviderController {

    private final OrderPaymentResultApplicationService orderPaymentResultApplicationService;
    private final OrderTimeoutApplicationService orderTimeoutApplicationService;

    public OrderCommandProviderController(
            OrderPaymentResultApplicationService orderPaymentResultApplicationService,
            OrderTimeoutApplicationService orderTimeoutApplicationService) {
        this.orderPaymentResultApplicationService = orderPaymentResultApplicationService;
        this.orderTimeoutApplicationService = orderTimeoutApplicationService;
    }

    @Operation(summary = "标记订单支付成功")
    @PostMapping("/mark-paid")
    public void markPaid(@Valid @RequestBody OrderMarkPaidFacadeRequest request) {
        orderPaymentResultApplicationService.markPaid(OrderInterfaceAssembler.toMarkPaidCommand(request));
    }

    @Operation(summary = "标记订单支付失败")
    @PostMapping("/mark-payment-failed")
    public void markPaymentFailed(@Valid @RequestBody OrderMarkPaymentFailedFacadeRequest request) {
        orderPaymentResultApplicationService.markPaymentFailed(
                OrderInterfaceAssembler.toMarkPaymentFailedCommand(request));
    }

    @Operation(summary = "关闭过期订单")
    @PostMapping("/close-expired")
    public void closeExpired(@Valid @RequestBody OrderCloseExpiredFacadeRequest request) {
        orderTimeoutApplicationService.closeExpiredOrder(OrderInterfaceAssembler.toCloseExpiredCommand(request));
    }
}
