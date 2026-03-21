package com.github.thundax.bacon.payment.interfaces.controller;

import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.application.service.PaymentQueryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentQueryController {

    private final PaymentQueryService paymentQueryService;

    public PaymentQueryController(PaymentQueryService paymentQueryService) {
        this.paymentQueryService = paymentQueryService;
    }

    @GetMapping("/{paymentNo}")
    public PaymentDetailDTO getByPaymentNo(@RequestParam("tenantId") Long tenantId, @PathVariable String paymentNo) {
        return paymentQueryService.getByPaymentNo(tenantId, paymentNo);
    }

    @GetMapping
    public PaymentDetailDTO getByOrderNo(@RequestParam("tenantId") Long tenantId, @RequestParam("orderNo") String orderNo) {
        return paymentQueryService.getByOrderNo(tenantId, orderNo);
    }
}
