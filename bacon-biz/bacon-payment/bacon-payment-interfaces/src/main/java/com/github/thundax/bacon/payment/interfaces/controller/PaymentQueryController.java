package com.github.thundax.bacon.payment.interfaces.controller;

import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.application.service.PaymentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payment", description = "支付查询接口")
public class PaymentQueryController {

    private final PaymentQueryService paymentQueryService;

    public PaymentQueryController(PaymentQueryService paymentQueryService) {
        this.paymentQueryService = paymentQueryService;
    }

    @Operation(summary = "按支付单号查询支付单")
    @HasPermission("payment:payment:view")
    @GetMapping("/{paymentNo}")
    public PaymentDetailDTO getByPaymentNo(@RequestParam("tenantId") Long tenantId, @PathVariable String paymentNo) {
        return paymentQueryService.getByPaymentNo(tenantId, paymentNo);
    }

    @Operation(summary = "按订单号查询支付单")
    @HasPermission("payment:payment:view")
    @GetMapping
    public PaymentDetailDTO getByOrderNo(@RequestParam("tenantId") Long tenantId, @RequestParam("orderNo") String orderNo) {
        return paymentQueryService.getByOrderNo(tenantId, orderNo);
    }
}
