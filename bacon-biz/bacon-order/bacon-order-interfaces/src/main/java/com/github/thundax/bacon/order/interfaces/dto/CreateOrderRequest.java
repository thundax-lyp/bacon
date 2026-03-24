package com.github.thundax.bacon.order.interfaces.dto;

import com.github.thundax.bacon.order.application.command.CreateOrderCommand;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "创建订单请求")
public record CreateOrderRequest(
        @Schema(description = "客户名称", example = "Alice") String customerName
) {

    public CreateOrderCommand toCommand() {
        return new CreateOrderCommand(customerName);
    }
}
