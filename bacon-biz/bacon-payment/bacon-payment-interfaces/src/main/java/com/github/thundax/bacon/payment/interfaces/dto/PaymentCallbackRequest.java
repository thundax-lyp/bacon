package com.github.thundax.bacon.payment.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付渠道回调请求")
public class PaymentCallbackRequest {

    @NotBlank
    @Schema(description = "租户编码", example = "TENANT_DEMO")
    private String tenantCode;

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

    @AssertTrue(
            message =
                    "Successful callback requires channelTransactionNo, channelStatus and rawPayload, and reason must be blank")
    public boolean isSuccessPayloadValid() {
        if (!isSuccess()) {
            return true;
        }
        return hasText(channelTransactionNo) && hasText(channelStatus) && hasText(rawPayload) && !hasText(reason);
    }

    @AssertTrue(message = "Failed callback requires channelStatus, rawPayload and reason")
    public boolean isFailurePayloadValid() {
        if (isSuccess()) {
            return true;
        }
        return hasText(channelStatus) && hasText(rawPayload) && hasText(reason);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
