package com.github.thundax.bacon.order.interfaces.provider;

import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.application.command.OrderPaymentResultApplicationService;
import com.github.thundax.bacon.order.application.command.OrderTimeoutApplicationService;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
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
@RequestMapping("/providers/orders")
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
    public OrderDetailDTO getById(@RequestParam("tenantId") Long tenantId, @PathVariable Long orderId) {
        return orderQueryService.getById(tenantId, orderId);
    }

    @GetMapping("/by-order-no/{orderNo}")
    public OrderDetailDTO getByOrderNo(@RequestParam("tenantId") Long tenantId, @PathVariable String orderNo) {
        return orderQueryService.getByOrderNo(tenantId, orderNo);
    }

    @GetMapping
    public OrderPageResultDTO pageOrders(OrderPageQueryDTO query) {
        return orderQueryService.pageOrders(query);
    }

    @PostMapping("/mark-paid")
    public void markPaid(
            @RequestParam("tenantId") Long tenantId,
            @RequestParam("orderNo") String orderNo,
            @RequestParam("paymentNo") String paymentNo,
            @RequestParam("channelCode") String channelCode,
            @RequestParam("paidAmount") BigDecimal paidAmount,
            @RequestParam("paidTime") Instant paidTime) {
        orderPaymentResultApplicationService.markPaid(tenantId, orderNo, paymentNo, channelCode, paidAmount, paidTime);
    }

    @PostMapping("/mark-payment-failed")
    public void markPaymentFailed(
            @RequestParam("tenantId") Long tenantId,
            @RequestParam("orderNo") String orderNo,
            @RequestParam("paymentNo") String paymentNo,
            @RequestParam("reason") String reason,
            @RequestParam("channelStatus") String channelStatus,
            @RequestParam("failedTime") Instant failedTime) {
        orderPaymentResultApplicationService.markPaymentFailed(
                tenantId, orderNo, paymentNo, reason, channelStatus, failedTime);
    }

    @PostMapping("/close-expired")
    public void closeExpired(
            @RequestParam("tenantId") Long tenantId,
            @RequestParam("orderNo") String orderNo,
            @RequestParam("reason") String reason) {
        orderTimeoutApplicationService.closeExpiredOrder(tenantId, orderNo, reason);
    }
}
