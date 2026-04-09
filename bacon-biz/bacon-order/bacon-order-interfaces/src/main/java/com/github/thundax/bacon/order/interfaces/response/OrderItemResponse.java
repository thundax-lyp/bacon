package com.github.thundax.bacon.order.interfaces.response;

import com.github.thundax.bacon.order.api.dto.OrderItemDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

/**
 * 订单项响应对象。
 */
@Schema(description = "订单项响应")
public record OrderItemResponse(
        /** 商品 SKU 主键。 */
        @Schema(description = "SKU ID", example = "101") Long skuId,
        /** 商品 SKU 名称。 */
        @Schema(description = "SKU 名称", example = "demo-item") String skuName,
        /** 商品图片快照。 */
        @Schema(description = "商品图片地址快照", example = "https://cdn.example.com/items/101.png") String imageUrl,
        /** 购买数量。 */
        @Schema(description = "数量", example = "1") Integer quantity,
        /** 销售价。 */
        @Schema(description = "销售价", example = "10.00") BigDecimal salePrice,
        /** 行金额。 */
        @Schema(description = "行金额", example = "10.00") BigDecimal lineAmount) {

    public static OrderItemResponse from(OrderItemDTO dto) {
        return new OrderItemResponse(
                dto.getSkuId(),
                dto.getSkuName(),
                dto.getImageUrl(),
                dto.getQuantity(),
                dto.getSalePrice(),
                dto.getLineAmount());
    }
}
