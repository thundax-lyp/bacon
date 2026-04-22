package com.github.thundax.bacon.order.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 关闭过期订单请求。
 */
public record OrderCloseExpiredRequest(
        @NotBlank @Size(max = 64) String orderNo, @NotBlank @Size(max = 255) String reason) {}
