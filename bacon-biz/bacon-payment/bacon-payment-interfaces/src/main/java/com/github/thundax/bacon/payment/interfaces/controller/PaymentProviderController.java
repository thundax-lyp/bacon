package com.github.thundax.bacon.payment.interfaces.controller;

import com.github.thundax.bacon.payment.api.dto.PaymentCloseResultDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.application.service.PaymentApplicationService;
import com.github.thundax.bacon.payment.application.service.PaymentCloseApplicationService;
import com.github.thundax.bacon.payment.application.service.PaymentQueryService;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/providers/payment")
public class PaymentProviderController {

    private final PaymentQueryService paymentQueryService;
    private final PaymentApplicationService paymentApplicationService;
    private final PaymentCloseApplicationService paymentCloseApplicationService;

    public PaymentProviderController(PaymentQueryService paymentQueryService,
                                     PaymentApplicationService paymentApplicationService,
                                     PaymentCloseApplicationService paymentCloseApplicationService) {
        this.paymentQueryService = paymentQueryService;
        this.paymentApplicationService = paymentApplicationService;
        this.paymentCloseApplicationService = paymentCloseApplicationService;
    }

    @GetMapping("/{paymentNo}")
    public PaymentDetailDTO getByPaymentNo(@RequestParam("tenantId") Long tenantId, @PathVariable String paymentNo) {
        return paymentQueryService.getByPaymentNo(tenantId, paymentNo);
    }

    @GetMapping
    public PaymentDetailDTO getByOrderNo(@RequestParam("tenantId") Long tenantId, @RequestParam("orderNo") String orderNo) {
        return paymentQueryService.getByOrderNo(tenantId, orderNo);
    }

    @PostMapping("/create")
    public PaymentCreateResultDTO createPayment(@RequestParam("tenantId") Long tenantId,
                                                @RequestParam("orderNo") String orderNo,
                                                @RequestParam("userId") Long userId,
                                                @RequestParam("amount") BigDecimal amount,
                                                @RequestParam("channelCode") String channelCode,
                                                @RequestParam("subject") String subject,
                                                @RequestParam("expiredAt") Instant expiredAt) {
        return paymentApplicationService.createPayment(tenantId, orderNo, userId, amount, channelCode, subject, expiredAt);
    }

    @PostMapping("/close")
    public PaymentCloseResultDTO closePayment(@RequestParam("tenantId") Long tenantId,
                                              @RequestParam("paymentNo") String paymentNo,
                                              @RequestParam("reason") String reason) {
        return paymentCloseApplicationService.closePayment(tenantId, paymentNo, reason);
    }
}
