package com.github.thundax.bacon.order.interfaces.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "订单视图对象")
public record OrderVO(
        @Schema(description = "订单ID", example = "1") Long id,
        @Schema(description = "订单号", example = "ORD202603230001") String orderNo,
        @Schema(description = "客户名称", example = "Alice") String customerName
) {
}
