package com.github.thundax.bacon.payment.interfaces.provider;

import com.github.thundax.bacon.payment.application.audit.PaymentAuditQueryApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCloseApplicationService;
import com.github.thundax.bacon.payment.application.command.PaymentCreateApplicationService;
import com.github.thundax.bacon.payment.application.query.PaymentQueryApplicationService;
import com.github.thundax.bacon.payment.interfaces.assembler.PaymentInterfaceAssembler;
import com.github.thundax.bacon.payment.interfaces.response.PaymentAuditLogResponse;
import com.github.thundax.bacon.payment.interfaces.response.PaymentCloseResponse;
import com.github.thundax.bacon.payment.interfaces.response.PaymentCreateResponse;
import com.github.thundax.bacon.payment.interfaces.response.PaymentDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/providers/payment")
@Tag(name = "Inner-Payment-Management", description = "Payment 域内部 Provider 接口")
public class PaymentProviderController {

    private final PaymentQueryApplicationService paymentQueryService;
    private final PaymentAuditQueryApplicationService paymentAuditQueryApplicationService;
    private final PaymentCreateApplicationService paymentCreateApplicationService;
    private final PaymentCloseApplicationService paymentCloseApplicationService;

    public PaymentProviderController(
            PaymentQueryApplicationService paymentQueryService,
            PaymentAuditQueryApplicationService paymentAuditQueryApplicationService,
            PaymentCreateApplicationService paymentCreateApplicationService,
            PaymentCloseApplicationService paymentCloseApplicationService) {
        this.paymentQueryService = paymentQueryService;
        this.paymentAuditQueryApplicationService = paymentAuditQueryApplicationService;
        this.paymentCreateApplicationService = paymentCreateApplicationService;
        this.paymentCloseApplicationService = paymentCloseApplicationService;
    }

    @Operation(summary = "按支付单号查询支付单")
    @GetMapping("/queries/detail")
    public PaymentDetailResponse getByPaymentNo(@RequestParam("paymentNo") @NotBlank String paymentNo) {
        return PaymentInterfaceAssembler.toDetailResponse(
                paymentQueryService.getByPaymentNo(PaymentInterfaceAssembler.toGetByPaymentNoQuery(paymentNo)));
    }

    @Operation(summary = "按订单号查询支付单")
    @GetMapping("/queries/by-order")
    public PaymentDetailResponse getByOrderNo(@RequestParam("orderNo") @NotBlank String orderNo) {
        return PaymentInterfaceAssembler.toDetailResponse(
                paymentQueryService.getByOrderNo(PaymentInterfaceAssembler.toGetByOrderNoQuery(orderNo)));
    }

    @Operation(summary = "按支付单号查询支付审计日志")
    @GetMapping("/queries/audit-logs")
    public List<PaymentAuditLogResponse> getAuditLogsByPaymentNo(
            @RequestParam("paymentNo") @NotBlank String paymentNo) {
        return PaymentInterfaceAssembler.toAuditLogResponses(
                paymentAuditQueryApplicationService.getByPaymentNo(PaymentInterfaceAssembler.toAuditLogQuery(paymentNo)));
    }

    @Operation(summary = "创建支付单")
    @PostMapping("/commands/create")
    public PaymentCreateResponse createPayment(
            @RequestParam("orderNo") @NotBlank String orderNo,
            @RequestParam("userId") @NotNull @Positive Long userId,
            @RequestParam("amount") @NotNull BigDecimal amount,
            @RequestParam("channelCode") @NotBlank String channelCode,
            @RequestParam("subject") @NotBlank String subject,
            @RequestParam("expiredAt") @NotNull Instant expiredAt) {
        return PaymentInterfaceAssembler.toCreateResponse(paymentCreateApplicationService.createPayment(
                PaymentInterfaceAssembler.toCreateCommand(orderNo, userId, amount, channelCode, subject, expiredAt)));
    }

    @Operation(summary = "关闭支付单")
    @PostMapping("/commands/close")
    public PaymentCloseResponse closePayment(
            @RequestParam("paymentNo") @NotBlank String paymentNo, @RequestParam("reason") @NotBlank String reason) {
        return PaymentInterfaceAssembler.toCloseResponse(
                paymentCloseApplicationService.closePayment(PaymentInterfaceAssembler.toCloseCommand(paymentNo, reason)));
    }
}
