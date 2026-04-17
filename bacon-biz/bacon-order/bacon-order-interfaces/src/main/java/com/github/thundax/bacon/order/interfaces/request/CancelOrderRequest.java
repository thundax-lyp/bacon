package com.github.thundax.bacon.order.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "取消订单请求")
public record CancelOrderRequest(
        @Size(max = 32) @Schema(description = "取消原因", example = "USER_CANCELLED") String reason,
        @Size(max = 32) @Schema(description = "操作人类型", example = "USER") String operatorType,
        @Size(max = 64) @Schema(description = "操作人ID", example = "u_2001") String operatorId) {}
