package com.github.thundax.bacon.order.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 标记订单支付成功请求。
 */
public record OrderMarkPaidRequest(
        @NotBlank @Size(max = 64) String orderNo,
        @NotBlank @Size(max = 64) String paymentNo,
        @NotBlank @Size(max = 32) String channelCode,
        @NotNull BigDecimal paidAmount,
        @NotNull Instant paidTime) {}
