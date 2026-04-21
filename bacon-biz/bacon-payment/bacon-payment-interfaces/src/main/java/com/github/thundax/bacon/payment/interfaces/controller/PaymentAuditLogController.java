package com.github.thundax.bacon.payment.interfaces.controller;

import com.github.thundax.bacon.common.security.annotation.HasPermission;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.payment.application.audit.PaymentAuditQueryApplicationService;
import com.github.thundax.bacon.payment.interfaces.assembler.PaymentInterfaceAssembler;
import com.github.thundax.bacon.payment.interfaces.response.PaymentAuditLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@WrappedApiController
@RequestMapping("/payment")
@Tag(name = "Payment-Audit", description = "支付审计日志查询接口")
public class PaymentAuditLogController {

    private final PaymentAuditQueryApplicationService paymentAuditQueryApplicationService;

    public PaymentAuditLogController(PaymentAuditQueryApplicationService paymentAuditQueryApplicationService) {
        this.paymentAuditQueryApplicationService = paymentAuditQueryApplicationService;
    }

    @Operation(summary = "按支付单号查询支付审计日志")
    @HasPermission("payment:payment:view")
    @GetMapping("/{paymentNo}/audit-logs")
    public List<PaymentAuditLogResponse> getByPaymentNo(@PathVariable("paymentNo") @NotBlank String paymentNo) {
        return PaymentInterfaceAssembler.toAuditLogResponses(
                paymentAuditQueryApplicationService.getByPaymentNo(PaymentInterfaceAssembler.toAuditLogQuery(paymentNo)));
    }
}
