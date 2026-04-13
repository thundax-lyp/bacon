package com.github.thundax.bacon.order.interfaces.response;

import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 订单摘要响应对象。
 */
@Schema(description = "订单摘要响应")
public record OrderSummaryResponse(
        /** 订单主键。 */
        @Schema(description = "订单ID", example = "1") Long id,
        /** 订单号。 */
        @Schema(description = "订单号", example = "ORD202603230001") String orderNo,
        /** 下单用户主键。 */
        @Schema(description = "用户ID", example = "2001") Long userId,
        /** 订单状态。 */
        @Schema(description = "订单状态", example = "CREATED") String orderStatus,
        /** 支付状态。 */
        @Schema(description = "支付状态", example = "UNPAID") String payStatus,
        /** 库存状态。 */
        @Schema(description = "库存状态", example = "UNRESERVED") String inventoryStatus,
        /** 支付单号。 */
        @Schema(description = "支付单号", example = "PAY202603230001") String paymentNo,
        /** 库存预占单号。 */
        @Schema(description = "预占单号", example = "RES202603230001") String reservationNo,
        /** 币种编码。 */
        @Schema(description = "币种", example = "CNY") String currencyCode,
        /** 订单总金额。 */
        @Schema(description = "订单总金额", example = "100.00") BigDecimal totalAmount,
        /** 应付金额。 */
        @Schema(description = "应付金额", example = "100.00") BigDecimal payableAmount,
        /** 取消原因。 */
        @Schema(description = "取消原因", example = "USER_CANCEL") String cancelReason,
        /** 关闭原因。 */
        @Schema(description = "关闭原因", example = "TIMEOUT") String closeReason,
        /** 创建时间。 */
        @Schema(description = "创建时间") Instant createdAt,
        /** 过期时间。 */
        @Schema(description = "过期时间") Instant expiredAt) {

    public static OrderSummaryResponse from(OrderSummaryDTO dto) {
        return new OrderSummaryResponse(
                dto.getId(),
                dto.getOrderNo(),
                dto.getUserId(),
                dto.getOrderStatus(),
                dto.getPayStatus(),
                dto.getInventoryStatus(),
                dto.getPaymentNo(),
                dto.getReservationNo(),
                dto.getCurrencyCode(),
                dto.getTotalAmount(),
                dto.getPayableAmount(),
                dto.getCancelReason(),
                dto.getCloseReason(),
                dto.getCreatedAt(),
                dto.getExpiredAt());
    }
}
