package com.github.thundax.bacon.order.interfaces.provider;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.commerce.codec.PaymentNoCodec;
import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.order.api.request.OrderPageFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderCloseExpiredFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaidFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderMarkPaymentFailedFacadeRequest;
import com.github.thundax.bacon.order.api.response.OrderDetailFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderPageFacadeResponse;
import com.github.thundax.bacon.order.application.command.OrderPaymentResultApplicationService;
import com.github.thundax.bacon.order.application.command.OrderTimeoutApplicationService;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.interfaces.assembler.OrderFacadeResponseAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/providers/order")
@Tag(name = "Inner-Order-Management", description = "Order 域内部 Provider 接口")
public class OrderReadProviderController {

    private final OrderQueryApplicationService orderQueryService;
    private final OrderPaymentResultApplicationService orderPaymentResultApplicationService;
    private final OrderTimeoutApplicationService orderTimeoutApplicationService;

    public OrderReadProviderController(
            OrderQueryApplicationService orderQueryService,
            OrderPaymentResultApplicationService orderPaymentResultApplicationService,
            OrderTimeoutApplicationService orderTimeoutApplicationService) {
        this.orderQueryService = orderQueryService;
        this.orderPaymentResultApplicationService = orderPaymentResultApplicationService;
        this.orderTimeoutApplicationService = orderTimeoutApplicationService;
    }

    @GetMapping("/{orderNo}")
    public OrderDetailFacadeResponse getByOrderNo(@PathVariable("orderNo") @NotBlank String orderNo) {
        return OrderFacadeResponseAssembler.fromDetailDto(orderQueryService.getByOrderNo(OrderNoCodec.toDomain(orderNo)));
    }

    @GetMapping
    public OrderPageFacadeResponse page(@Valid OrderPageFacadeRequest request) {
        return OrderFacadeResponseAssembler.fromPageDto(orderQueryService.page(
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
