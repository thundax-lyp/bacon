package com.github.thundax.bacon.order.application.assembler;

import com.github.thundax.bacon.common.commerce.codec.OrderNoCodec;
import com.github.thundax.bacon.common.commerce.codec.PaymentNoCodec;
import com.github.thundax.bacon.common.commerce.codec.SkuIdCodec;
import com.github.thundax.bacon.common.id.codec.UserIdCodec;
import com.github.thundax.bacon.order.application.codec.OrderIdCodec;
import com.github.thundax.bacon.order.application.codec.ReservationNoCodec;
import com.github.thundax.bacon.order.application.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.application.dto.OrderItemDTO;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderPaymentSnapshot;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class OrderDetailAssembler {

    public OrderDetailDTO toDto(
            Order order,
            List<OrderItem> items,
            OrderPaymentSnapshot paymentSnapshot,
            OrderInventorySnapshot inventorySnapshot) {
        List<OrderItemDTO> itemDtos = items.stream()
                .map(item -> new OrderItemDTO(
                        SkuIdCodec.toValue(item.getSkuId()),
                        item.getSkuName(),
                        item.getImageUrl(),
                        item.getQuantity(),
                        item.getSalePrice().value(),
                        item.getLineAmount().value()))
                .toList();
        return new OrderDetailDTO(
                order.getId() == null ? null : OrderIdCodec.toValue(order.getId()),
                OrderNoCodec.toValue(order.getOrderNo()),
                UserIdCodec.toValue(order.getUserId()),
                order.getOrderStatus() == null ? null : order.getOrderStatus().value(),
                order.getPayStatus() == null ? null : order.getPayStatus().value(),
                order.getInventoryStatus() == null ? null : order.getInventoryStatus().value(),
                paymentSnapshot == null
                        ? PaymentNoCodec.toValue(order.getPaymentNo())
                        : (paymentSnapshot.paymentNo() == null
                                ? null
                                : paymentSnapshot.paymentNo().value()),
                inventorySnapshot == null
                        ? (order.getReservationNo() == null
                                ? null
                                : ReservationNoCodec.toValue(order.getReservationNo()))
                        : (inventorySnapshot.reservationNo() == null
                                ? null
                                : ReservationNoCodec.toValue(inventorySnapshot.reservationNo())),
                order.getCurrencyCode() == null ? null : order.getCurrencyCode().value(),
                order.getTotalAmount().value(),
                order.getPayableAmount().value(),
                order.getCancelReason(),
                order.getCloseReason(),
                order.getCreatedAt(),
                order.getExpiredAt(),
                itemDtos,
                buildPaymentSnapshot(order, paymentSnapshot),
                buildInventorySnapshot(order, inventorySnapshot),
                order.getPaidAt(),
                order.getClosedAt());
    }

    private String buildPaymentSnapshot(Order order, OrderPaymentSnapshot paymentSnapshot) {
        if (paymentSnapshot == null && order.getPaymentNo() == null) {
            return null;
        }
        String paymentNo = paymentSnapshot == null
                ? PaymentNoCodec.toValue(order.getPaymentNo())
                : (paymentSnapshot.paymentNo() == null ? null : paymentSnapshot.paymentNo().value());
        String payStatus = paymentSnapshot == null
                ? (order.getPayStatus() == null ? null : order.getPayStatus().value())
                : (paymentSnapshot.payStatus() == null ? null : paymentSnapshot.payStatus().value());
        String channelCode = paymentSnapshot == null
                ? (order.getPaymentChannelCode() == null ? null : order.getPaymentChannelCode().value())
                : (paymentSnapshot.channelCode() == null ? null : paymentSnapshot.channelCode().value());
        BigDecimal paidAmount = paymentSnapshot == null
                ? (order.getPaidAmount() == null ? null : order.getPaidAmount().value())
                : (paymentSnapshot.paidAmount() == null ? null : paymentSnapshot.paidAmount().value());
        String channelStatus = paymentSnapshot == null
                ? order.getPaymentChannelStatus()
                : (paymentSnapshot.channelStatus() == null ? null : paymentSnapshot.channelStatus().value());
        String failureReason =
                paymentSnapshot == null ? order.getPaymentFailureReason() : paymentSnapshot.failureReason();
        return "paymentNo=" + paymentNo
                + ",payStatus=" + payStatus
                + ",channelCode=" + Objects.toString(channelCode, "N/A")
                + ",paidAmount=" + Objects.toString(paidAmount, "N/A")
                + ",channelStatus=" + Objects.toString(channelStatus, "N/A")
                + ",failureReason=" + Objects.toString(failureReason, "N/A");
    }

    private String buildInventorySnapshot(Order order, OrderInventorySnapshot inventorySnapshot) {
        String reservationNo = Objects.toString(
                inventorySnapshot == null
                        ? (order.getReservationNo() == null
                                ? null
                                : ReservationNoCodec.toValue(order.getReservationNo()))
                        : (inventorySnapshot.reservationNo() == null
                                ? null
                                : ReservationNoCodec.toValue(inventorySnapshot.reservationNo())),
                "N/A");
        String inventoryStatus = inventorySnapshot == null
                ? (order.getInventoryStatus() == null ? null : order.getInventoryStatus().value())
                : (inventorySnapshot.inventoryStatus() == null ? null : inventorySnapshot.inventoryStatus().value());
        String warehouseCode = inventorySnapshot == null
                ? (order.getWarehouseCode() == null ? null : order.getWarehouseCode().value())
                : (inventorySnapshot.warehouseCode() == null ? null : inventorySnapshot.warehouseCode().value());
        String failureReason =
                inventorySnapshot == null ? order.getInventoryFailureReason() : inventorySnapshot.failureReason();
        return "reservationNo=" + reservationNo
                + ",inventoryStatus=" + inventoryStatus
                + ",warehouseCode=" + Objects.toString(warehouseCode, "N/A")
                + ",failureReason=" + Objects.toString(failureReason, "N/A");
    }
}
