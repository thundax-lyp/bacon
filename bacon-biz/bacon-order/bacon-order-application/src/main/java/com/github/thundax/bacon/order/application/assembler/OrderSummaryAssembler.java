package com.github.thundax.bacon.order.application.assembler;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.commerce.codec.PaymentNoCodec;
import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.order.application.codec.OrderIdCodec;
import com.github.thundax.bacon.order.application.codec.ReservationNoCodec;
import com.github.thundax.bacon.order.application.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderSummaryAssembler {

    public OrderSummaryDTO toDto(Order order) {
        return new OrderSummaryDTO(
                order.getId() == null ? null : OrderIdCodec.toValue(order.getId()),
                OrderNoCodec.toValue(order.getOrderNo()),
                UserIdCodec.toValue(order.getUserId()),
                order.getOrderStatus() == null ? null : order.getOrderStatus().value(),
                order.getPayStatus() == null ? null : order.getPayStatus().value(),
                order.getInventoryStatus() == null ? null : order.getInventoryStatus().value(),
                PaymentNoCodec.toValue(order.getPaymentNo()),
                order.getReservationNo() == null ? null : ReservationNoCodec.toValue(order.getReservationNo()),
                order.getCurrencyCode() == null ? null : order.getCurrencyCode().value(),
                order.getTotalAmount().value(),
                order.getPayableAmount().value(),
                order.getCancelReason(),
                order.getCloseReason(),
                order.getCreatedAt(),
                order.getExpiredAt());
    }
}
