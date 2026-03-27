package com.github.thundax.bacon.payment.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付渠道回调请求")
public class PaymentCallbackRequest {

    @NotNull
    @Positive
    @Schema(description = "租户ID", example = "1001")
    private Long tenantId;

    @NotBlank
    @Schema(description = "支付单号", example = "PAY202603230001")
    private String paymentNo;

    @NotNull
    @Schema(description = "是否成功", example = "true")
    private Boolean success;

    @Schema(description = "渠道状态", example = "SUCCESS")
    private String channelStatus;

    @Schema(description = "原始回调载荷", example = "{\"tradeStatus\":\"SUCCESS\"}")
    private String rawPayload;

    @Schema(description = "失败原因", example = "CHANNEL_TIMEOUT")
    private String reason;

    @Schema(description = "渠道交易流水号", example = "WX202603230001")
    private String channelTransactionNo;

    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }
}
