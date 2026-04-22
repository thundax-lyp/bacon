package com.github.thundax.bacon.order.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * 标记订单支付失败请求。
 */
public record OrderMarkPaymentFailedRequest(
        @NotBlank @Size(max = 64) String orderNo,
        @NotBlank @Size(max = 64) String paymentNo,
        @NotBlank @Size(max = 255) String reason,
        @NotBlank @Size(max = 64) String channelStatus,
        @NotNull Instant failedTime) {}
