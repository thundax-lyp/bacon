package com.github.thundax.bacon.order.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

@Schema(description = "创建订单请求")
public record CreateOrderRequest(
        @Schema(description = "用户ID", example = "2001") Long userId,
        @Size(max = 16) @Schema(description = "币种编码", example = "CNY") String currencyCode,
        @Size(max = 32) @Schema(description = "渠道编码", example = "MOCK") String channelCode,
        @Size(max = 255) @Schema(description = "备注", example = "normal-order") String remark,
        @Schema(description = "过期时间") Instant expiredAt,
        @Schema(description = "订单项") List<CreateOrderItemRequest> items) {}
