package com.github.thundax.bacon.order.interfaces.provider;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.commerce.codec.PaymentNoCodec;
import com.github.thundax.bacon.order.api.request.OrderCloseExpiredFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaidFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaymentFailedFacadeRequest;
import com.github.thundax.bacon.order.application.command.OrderPaymentResultApplicationService;
import com.github.thundax.bacon.order.application.command.OrderTimeoutApplicationService;
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

    @PostMapping("/mark-paid")
    public void markPaid(@Valid @RequestBody OrderMarkPaidFacadeRequest request) {
        orderPaymentResultApplicationService.markPaid(
                OrderNoCodec.toDomain(request.getOrderNo()),
                PaymentNoCodec.toDomain(request.getPaymentNo()),
                request.getChannelCode(),
                request.getPaidAmount(),
                request.getPaidTime());
    }

    @PostMapping("/mark-payment-failed")
    public void markPaymentFailed(@Valid @RequestBody OrderMarkPaymentFailedFacadeRequest request) {
        orderPaymentResultApplicationService.markPaymentFailed(
                OrderNoCodec.toDomain(request.getOrderNo()),
                PaymentNoCodec.toDomain(request.getPaymentNo()),
                request.getReason(),
                request.getChannelStatus(),
                request.getFailedTime());
    }

    @PostMapping("/close-expired")
    public void closeExpired(@Valid @RequestBody OrderCloseExpiredFacadeRequest request) {
        orderTimeoutApplicationService.closeExpiredOrder(OrderNoCodec.toDomain(request.getOrderNo()), request.getReason());
    }
}
