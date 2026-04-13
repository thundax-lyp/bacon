package com.github.thundax.bacon.order.interfaces.dto;

import com.github.thundax.bacon.order.application.command.CreateOrderCommand;
import com.github.thundax.bacon.order.application.command.CreateOrderItemCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "创建订单请求")
public record CreateOrderRequest(
        @Schema(description = "用户ID", example = "2001") Long userId,
        @Schema(description = "币种编码", example = "CNY") String currencyCode,
        @Schema(description = "渠道编码", example = "MOCK") String channelCode,
        @Schema(description = "备注", example = "normal-order") String remark,
        @Schema(description = "过期时间") Instant expiredAt,
        @Schema(description = "订单项") List<CreateOrderItemRequest> items) {

    public CreateOrderCommand toCommand() {
        List<CreateOrderItemCommand> itemCommands = items == null
                ? List.of()
                : items.stream().map(CreateOrderItemRequest::toCommand).toList();
        return new CreateOrderCommand(userId, currencyCode, channelCode, remark, expiredAt, itemCommands);
    }
}
