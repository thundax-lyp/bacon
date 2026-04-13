package com.github.thundax.bacon.order.interfaces.response;

import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 订单详情响应对象。
 */
@Schema(description = "订单详情响应")
public record OrderDetailResponse(
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
        @Schema(description = "过期时间") Instant expiredAt,
        /** 订单项列表。 */
        @Schema(description = "订单项") List<OrderItemResponse> items,
        /** 支付快照摘要。 */
        @Schema(description = "支付快照", example = "mock-payment") String paymentSnapshot,
        /** 库存快照摘要。 */
        @Schema(description = "库存快照", example = "mock-inventory") String inventorySnapshot,
        /** 支付完成时间。 */
        @Schema(description = "支付时间") Instant paidAt,
        /** 关闭时间。 */
        @Schema(description = "关闭时间") Instant closedAt) {

    public static OrderDetailResponse from(OrderDetailDTO dto) {
        List<OrderItemResponse> itemResponses = dto.getItems() == null
                ? List.of()
                : dto.getItems().stream().map(OrderItemResponse::from).toList();
        return new OrderDetailResponse(
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
                dto.getExpiredAt(),
                itemResponses,
                dto.getPaymentSnapshot(),
                dto.getInventorySnapshot(),
                dto.getPaidAt(),
                dto.getClosedAt());
    }
}
