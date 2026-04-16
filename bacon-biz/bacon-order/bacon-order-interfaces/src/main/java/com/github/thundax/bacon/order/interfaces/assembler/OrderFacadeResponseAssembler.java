package com.github.thundax.bacon.order.interfaces.assembler;

import com.github.thundax.bacon.order.api.response.OrderDetailFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderItemFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderPageFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderSummaryFacadeResponse;
import com.github.thundax.bacon.order.application.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.application.dto.OrderItemDTO;
import com.github.thundax.bacon.order.application.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.result.OrderPageResult;
import java.util.List;

public final class OrderFacadeResponseAssembler {

    private OrderFacadeResponseAssembler() {}

    public static OrderDetailFacadeResponse fromDetailDto(OrderDetailDTO dto) {
        if (dto == null) {
            return null;
        }
        List<OrderItemFacadeResponse> items = dto.getItems() == null
                ? List.of()
                : dto.getItems().stream().map(OrderFacadeResponseAssembler::fromItemDto).toList();
        return new OrderDetailFacadeResponse(
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
                items,
                dto.getPaymentSnapshot(),
                dto.getInventorySnapshot(),
                dto.getPaidAt(),
                dto.getClosedAt());
    }

    public static OrderPageFacadeResponse fromPageDto(OrderPageResult dto) {
        if (dto == null) {
            return null;
        }
        List<OrderSummaryFacadeResponse> records = dto.getRecords() == null
                ? List.of()
                : dto.getRecords().stream().map(OrderFacadeResponseAssembler::fromSummaryDto).toList();
        return new OrderPageFacadeResponse(records, dto.getTotal(), dto.getPageNo(), dto.getPageSize());
    }

    private static OrderItemFacadeResponse fromItemDto(OrderItemDTO dto) {
        return new OrderItemFacadeResponse(
                dto.getSkuId(),
                dto.getSkuName(),
                dto.getImageUrl(),
                dto.getQuantity(),
                dto.getSalePrice(),
                dto.getLineAmount());
    }

    private static OrderSummaryFacadeResponse fromSummaryDto(OrderSummaryDTO dto) {
        return new OrderSummaryFacadeResponse(
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
