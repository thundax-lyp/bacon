package com.github.thundax.bacon.order.interfaces.response;

import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "订单摘要响应")
public record OrderSummaryResponse(
        @Schema(description = "订单ID", example = "1") Long id,
        @Schema(description = "租户ID", example = "1001") Long tenantId,
        @Schema(description = "订单号", example = "ORD202603230001") String orderNo,
        @Schema(description = "用户ID", example = "2001") Long userId,
        @Schema(description = "订单状态", example = "CREATED") String orderStatus,
        @Schema(description = "支付状态", example = "UNPAID") String payStatus,
        @Schema(description = "库存状态", example = "UNRESERVED") String inventoryStatus,
        @Schema(description = "支付单号", example = "PAY202603230001") String paymentNo,
        @Schema(description = "预占单号", example = "RES202603230001") String reservationNo,
        @Schema(description = "币种", example = "CNY") String currencyCode,
        @Schema(description = "订单总金额", example = "100.00") BigDecimal totalAmount,
        @Schema(description = "应付金额", example = "100.00") BigDecimal payableAmount,
        @Schema(description = "取消原因", example = "USER_CANCEL") String cancelReason,
        @Schema(description = "关闭原因", example = "TIMEOUT") String closeReason,
        @Schema(description = "创建时间") Instant createdAt,
        @Schema(description = "过期时间") Instant expiredAt) {

    public static OrderSummaryResponse from(OrderSummaryDTO dto) {
        return new OrderSummaryResponse(dto.getId(), dto.getTenantId(), dto.getOrderNo(), dto.getUserId(),
                dto.getOrderStatus(), dto.getPayStatus(), dto.getInventoryStatus(), dto.getPaymentNo(),
                dto.getReservationNo(), dto.getCurrencyCode(), dto.getTotalAmount(), dto.getPayableAmount(),
                dto.getCancelReason(), dto.getCloseReason(), dto.getCreatedAt(), dto.getExpiredAt());
    }
}
