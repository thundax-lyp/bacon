package com.github.thundax.bacon.payment.interfaces.provider;

import com.github.thundax.bacon.payment.api.dto.PaymentCloseResultDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.application.service.PaymentApplicationService;
import com.github.thundax.bacon.payment.application.service.PaymentCloseApplicationService;
import com.github.thundax.bacon.payment.application.service.PaymentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/providers/payment")
@Tag(name = "Inner-Payment-Management", description = "Payment 域内部 Provider 接口")
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

    @Operation(summary = "按支付单号查询支付单")
    @GetMapping("/{paymentNo}")
    public PaymentDetailDTO getByPaymentNo(@RequestParam("tenantId") Long tenantId,
                                           @PathVariable String paymentNo) {
        return paymentQueryService.getByPaymentNo(tenantId, paymentNo);
    }

    @Operation(summary = "按订单号查询支付单")
    @GetMapping
    public PaymentDetailDTO getByOrderNo(@RequestParam("tenantId") Long tenantId,
                                         @RequestParam("orderNo") String orderNo) {
        return paymentQueryService.getByOrderNo(tenantId, orderNo);
    }

    @Operation(summary = "创建支付单")
    @PostMapping("/create")
    public PaymentCreateResultDTO createPayment(@RequestParam("tenantId") Long tenantId,
                                                @RequestParam("orderNo") String orderNo,
                                                @RequestParam("userId") Long userId,
                                                @RequestParam("amount") BigDecimal amount,
                                                @RequestParam("channelCode") String channelCode,
                                                @RequestParam("subject") String subject,
                                                @RequestParam("expiredAt") Instant expiredAt) {
        return paymentApplicationService.createPayment(tenantId, orderNo, userId, amount,
                channelCode, subject, expiredAt);
    }

    @Operation(summary = "关闭支付单")
    @PostMapping("/close")
    public PaymentCloseResultDTO closePayment(@RequestParam("tenantId") Long tenantId,
                                              @RequestParam("paymentNo") String paymentNo,
                                              @RequestParam("reason") String reason) {
        return paymentCloseApplicationService.closePayment(tenantId, paymentNo, reason);
    }
}
