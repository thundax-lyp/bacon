package com.github.thundax.bacon.payment.interfaces.controller;

import com.github.thundax.bacon.payment.application.command.PaymentCallbackApplicationService;
import com.github.thundax.bacon.payment.interfaces.dto.PaymentCallbackRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/payment/callbacks")
@Tag(name = "Payment-Callback", description = "支付回调接口")
public class PaymentCallbackController {

    private final PaymentCallbackApplicationService paymentCallbackApplicationService;

    public PaymentCallbackController(PaymentCallbackApplicationService paymentCallbackApplicationService) {
        this.paymentCallbackApplicationService = paymentCallbackApplicationService;
    }

    @Operation(summary = "处理支付渠道回调")
    @PostMapping("/{channelCode}")
    public void callback(
            @PathVariable("channelCode") @NotBlank String channelCode,
            @Valid @RequestBody PaymentCallbackRequest request) {
        if (request.isSuccess()) {
            paymentCallbackApplicationService.callbackPaid(
                    channelCode,
                    request.getPaymentNo(),
                    request.getChannelTransactionNo(),
                    request.getChannelStatus(),
                    request.getRawPayload());
            return;
        }
        paymentCallbackApplicationService.callbackFailed(
                channelCode,
                request.getPaymentNo(),
                request.getChannelStatus(),
                request.getRawPayload(),
                request.getReason());
    }
}
