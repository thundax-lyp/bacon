package com.github.thundax.bacon.order.interfaces.response;

import com.github.thundax.bacon.order.api.dto.OrderItemDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "订单项响应")
public record OrderItemResponse(
        @Schema(description = "SKU ID", example = "101") Long skuId,
        @Schema(description = "SKU 名称", example = "demo-item") String skuName,
        @Schema(description = "数量", example = "1") Integer quantity,
        @Schema(description = "销售价", example = "10.00") BigDecimal salePrice,
        @Schema(description = "行金额", example = "10.00") BigDecimal lineAmount) {

    public static OrderItemResponse from(OrderItemDTO dto) {
        return new OrderItemResponse(dto.getSkuId(), dto.getSkuName(), dto.getQuantity(), dto.getSalePrice(),
                dto.getLineAmount());
    }
}
