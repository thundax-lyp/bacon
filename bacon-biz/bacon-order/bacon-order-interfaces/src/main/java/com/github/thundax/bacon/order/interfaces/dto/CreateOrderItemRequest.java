package com.github.thundax.bacon.order.interfaces.dto;

import com.github.thundax.bacon.order.application.command.CreateOrderItemCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "创建订单项请求")
public record CreateOrderItemRequest(
        @Schema(description = "SKU ID", example = "101") Long skuId,
        @Schema(description = "SKU 名称", example = "demo-item") String skuName,
        @Schema(description = "商品图片地址快照", example = "https://cdn.example.com/items/101.png") String imageUrl,
        @Schema(description = "数量", example = "1") Integer quantity,
        @Schema(description = "销售价", example = "10.00") BigDecimal salePrice) {

    public CreateOrderItemCommand toCommand() {
        return new CreateOrderItemCommand(skuId, skuName, imageUrl, quantity, salePrice);
    }
}
