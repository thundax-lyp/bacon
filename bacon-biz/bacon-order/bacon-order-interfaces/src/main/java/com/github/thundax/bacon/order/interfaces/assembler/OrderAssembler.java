package com.github.thundax.bacon.order.interfaces.assembler;

import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.command.CreateOrderCommand;
import com.github.thundax.bacon.order.interfaces.dto.CreateOrderRequest;
import com.github.thundax.bacon.order.interfaces.response.OrderSummaryResponse;

public final class OrderAssembler {

    private OrderAssembler() {
    }

    public static CreateOrderCommand toCommand(CreateOrderRequest request) {
        return new CreateOrderCommand(request.orderNo(), request.customerName());
    }

    public static OrderSummaryResponse toResponse(OrderSummaryDTO summaryDTO) {
        return OrderSummaryResponse.from(summaryDTO);
    }
}
