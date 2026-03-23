package com.github.thundax.bacon.payment.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付渠道回调请求")
public class PaymentCallbackRequest {

    @Schema(description = "租户ID", example = "1001")
    private Long tenantId;

    @Schema(description = "支付单号", example = "PAY202603230001")
    private String paymentNo;

    @Schema(description = "回调结果", example = "SUCCESS")
    private String result;

    @Schema(description = "失败原因", example = "CHANNEL_TIMEOUT")
    private String reason;

    @Schema(description = "渠道交易流水号", example = "WX202603230001")
    private String channelTransactionNo;
}
