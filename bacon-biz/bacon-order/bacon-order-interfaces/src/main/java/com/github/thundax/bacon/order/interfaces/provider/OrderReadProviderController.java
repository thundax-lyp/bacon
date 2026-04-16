package com.github.thundax.bacon.order.interfaces.provider;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.commerce.codec.PaymentNoCodec;
import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.api.query.OrderPageQuery;
import com.github.thundax.bacon.order.application.codec.OrderIdCodec;
import com.github.thundax.bacon.order.application.command.OrderPaymentResultApplicationService;
import com.github.thundax.bacon.order.application.command.OrderTimeoutApplicationService;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
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

    @GetMapping("/{orderId}")
    public OrderDetailDTO getById(@PathVariable Long orderId) {
        return orderQueryService.getById(OrderIdCodec.toDomain(orderId));
    }

    @GetMapping("/by-order-no/{orderNo}")
    public OrderDetailDTO getByOrderNo(@PathVariable String orderNo) {
        return orderQueryService.getByOrderNo(OrderNoCodec.toDomain(orderNo));
    }

    @GetMapping
    public OrderPageResultDTO pageOrders(OrderPageQuery query) {
        return orderQueryService.pageOrders(
                UserIdCodec.toDomain(query.getUserId()),
                OrderNoCodec.toDomain(query.getOrderNo()),
                query.getOrderStatus() == null ? null : OrderStatus.from(query.getOrderStatus()),
                query.getPayStatus() == null ? null : PayStatus.from(query.getPayStatus()),
                query.getInventoryStatus() == null ? null : InventoryStatus.from(query.getInventoryStatus()),
                query.getCreatedAtFrom(),
                query.getCreatedAtTo(),
                query.getPageNo(),
                query.getPageSize());
    }

    @PostMapping("/mark-paid")
    public void markPaid(
            @RequestParam("orderNo") String orderNo,
            @RequestParam("paymentNo") String paymentNo,
            @RequestParam("channelCode") String channelCode,
            @RequestParam("paidAmount") BigDecimal paidAmount,
            @RequestParam("paidTime") Instant paidTime) {
        orderPaymentResultApplicationService.markPaid(
                OrderNoCodec.toDomain(orderNo), PaymentNoCodec.toDomain(paymentNo), channelCode, paidAmount, paidTime);
    }

    @PostMapping("/mark-payment-failed")
    public void markPaymentFailed(
            @RequestParam("orderNo") String orderNo,
            @RequestParam("paymentNo") String paymentNo,
            @RequestParam("reason") String reason,
            @RequestParam("channelStatus") String channelStatus,
            @RequestParam("failedTime") Instant failedTime) {
        orderPaymentResultApplicationService.markPaymentFailed(
                OrderNoCodec.toDomain(orderNo), PaymentNoCodec.toDomain(paymentNo), reason, channelStatus, failedTime);
    }

    @PostMapping("/close-expired")
    public void closeExpired(@RequestParam("orderNo") String orderNo, @RequestParam("reason") String reason) {
        orderTimeoutApplicationService.closeExpiredOrder(OrderNoCodec.toDomain(orderNo), reason);
    }
}
