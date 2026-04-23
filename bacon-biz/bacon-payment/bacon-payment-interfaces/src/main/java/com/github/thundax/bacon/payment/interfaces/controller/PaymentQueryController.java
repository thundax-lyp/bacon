package com.github.thundax.bacon.payment.interfaces.controller;

import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.payment.application.query.PaymentQueryApplicationService;
import com.github.thundax.bacon.payment.interfaces.assembler.PaymentInterfaceAssembler;
import com.github.thundax.bacon.payment.interfaces.response.PaymentDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@WrappedApiController
@RequestMapping("/payment/payments")
@Tag(name = "Payment-Query", description = "支付查询接口")
public class PaymentQueryController {

    private final PaymentQueryApplicationService paymentQueryService;

    public PaymentQueryController(PaymentQueryApplicationService paymentQueryService) {
        this.paymentQueryService = paymentQueryService;
    }

    @Operation(summary = "按支付单号查询支付单")
    @HasPermission("payment:payment:view")
    @GetMapping("/{paymentNo}")
    public PaymentDetailResponse getByPaymentNo(@PathVariable("paymentNo") @NotBlank String paymentNo) {
        return PaymentInterfaceAssembler.toDetailResponse(
                paymentQueryService.getByPaymentNo(PaymentInterfaceAssembler.toGetByPaymentNoQuery(paymentNo)));
    }

    @Operation(summary = "按订单号查询支付单")
    @HasPermission("payment:payment:view")
    @GetMapping
    public PaymentDetailResponse getByOrderNo(@RequestParam("orderNo") @NotBlank String orderNo) {
        return PaymentInterfaceAssembler.toDetailResponse(
                paymentQueryService.getByOrderNo(PaymentInterfaceAssembler.toGetByOrderNoQuery(orderNo)));
    }
}
