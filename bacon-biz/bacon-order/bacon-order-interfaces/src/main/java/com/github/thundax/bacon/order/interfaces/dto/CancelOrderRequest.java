package com.github.thundax.bacon.order.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "取消订单请求")
public record CancelOrderRequest(
        @Schema(description = "租户ID", example = "1001") Long tenantId,
        @Schema(description = "取消原因", example = "USER_CANCELLED") String reason,
        @Schema(description = "操作人类型", example = "USER") String operatorType,
        @Schema(description = "操作人ID", example = "u_2001") String operatorId
) {
}
