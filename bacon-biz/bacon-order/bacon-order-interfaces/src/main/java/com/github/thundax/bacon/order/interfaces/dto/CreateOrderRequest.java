package com.github.thundax.bacon.order.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "创建订单请求")
public record CreateOrderRequest(
        @Schema(description = "订单号", example = "ORD202603230001") String orderNo,
        @Schema(description = "客户名称", example = "Alice") String customerName
) {
}
