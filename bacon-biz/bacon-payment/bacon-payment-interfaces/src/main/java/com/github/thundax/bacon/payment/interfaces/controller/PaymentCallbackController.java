package com.github.thundax.bacon.payment.interfaces.controller;

import com.github.thundax.bacon.payment.application.service.PaymentCallbackApplicationService;
import com.github.thundax.bacon.payment.interfaces.dto.PaymentCallbackRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments/callback")
public class PaymentCallbackController {

    private final PaymentCallbackApplicationService paymentCallbackApplicationService;

    public PaymentCallbackController(PaymentCallbackApplicationService paymentCallbackApplicationService) {
        this.paymentCallbackApplicationService = paymentCallbackApplicationService;
    }

    @PostMapping("/{channelCode}")
    public void callback(@PathVariable String channelCode, @RequestBody PaymentCallbackRequest request) {
        if ("SUCCESS".equalsIgnoreCase(request.getResult())) {
            paymentCallbackApplicationService.callbackPaid(channelCode, request.getTenantId(),
                    request.getPaymentNo(), request.getChannelTransactionNo());
            return;
        }
        paymentCallbackApplicationService.callbackFailed(channelCode, request.getTenantId(),
                request.getPaymentNo(), request.getReason());
    }
}
