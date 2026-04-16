package com.github.thundax.bacon.order.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单支付失败回写门面请求。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMarkPaymentFailedFacadeRequest {

    @NotBlank
    @Size(max = 64)
    private String orderNo;

    @NotBlank
    @Size(max = 64)
    private String paymentNo;

    @NotBlank
    @Size(max = 255)
    private String reason;

    @NotBlank
    @Size(max = 64)
    private String channelStatus;

    @NotNull
    private Instant failedTime;
}
