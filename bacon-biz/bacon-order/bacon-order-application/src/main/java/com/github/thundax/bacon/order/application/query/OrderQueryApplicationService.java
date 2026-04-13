package com.github.thundax.bacon.order.application.query;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderItemDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.codec.OrderIdCodec;
import com.github.thundax.bacon.order.application.codec.ReservationNoCodec;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannel;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class OrderQueryApplicationService {

    private final OrderRepository orderRepository;

    public OrderQueryApplicationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderDetailDTO getById(Long orderId) {
        BaconContextHolder.requireTenantId();
        return orderRepository
                .findById(orderId)
                .map(this::toDetail)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }

    public OrderDetailDTO getByOrderNo(String orderNo) {
        BaconContextHolder.requireTenantId();
        return orderRepository
                .findByOrderNo(orderNo)
                .map(this::toDetail)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
    }

    public OrderPageResultDTO pageOrders(OrderPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        long total = orderRepository.countOrders(
                query.getUserId(),
                query.getOrderNo(),
                query.getOrderStatus(),
                query.getPayStatus(),
                query.getInventoryStatus(),
                query.getCreatedAtFrom(),
                query.getCreatedAtTo());
        List<Order> pageOrders = total <= 0
                ? List.of()
                : orderRepository.pageOrders(
                        query.getUserId(),
                        query.getOrderNo(),
                        query.getOrderStatus(),
                        query.getPayStatus(),
                        query.getInventoryStatus(),
                        query.getCreatedAtFrom(),
                        query.getCreatedAtTo(),
                        offset,
                        pageSize);
        List<OrderSummaryDTO> records = pageOrders.stream().map(this::toSummary).toList();
        return new OrderPageResultDTO(records, total, pageNo, pageSize);
    }

    private OrderSummaryDTO toSummary(Order order) {
        Long tenantId = BaconContextHolder.currentTenantId();
        return new OrderSummaryDTO(
                order.getId() == null ? null : OrderIdCodec.toValue(order.getId()),
                tenantId,
                order.getOrderNo() == null ? null : order.getOrderNo().value(),
                order.getUserId() == null ? null : order.getUserId().value(),
                order.getOrderStatus() == null ? null : order.getOrderStatus().value(),
                order.getPayStatus() == null ? null : order.getPayStatus().value(),
                order.getInventoryStatus() == null ? null : order.getInventoryStatus().value(),
                order.getPaymentNo() == null ? null : order.getPaymentNo().value(),
                order.getReservationNo() == null ? null : ReservationNoCodec.toValue(order.getReservationNo()),
                order.getCurrencyCode() == null ? null : order.getCurrencyCode().value(),
                order.getTotalAmount().value(),
                order.getPayableAmount().value(),
                order.getCancelReason(),
                order.getCloseReason(),
                order.getCreatedAt(),
                order.getExpiredAt());
    }

    private OrderDetailDTO toDetail(Order order) {
        Long tenantId = BaconContextHolder.currentTenantId();
        OrderPaymentSnapshot paymentSnapshot = orderRepository
                .findPaymentSnapshotByOrderId(order.getId() == null ? null : OrderIdCodec.toValue(order.getId()))
                .orElse(null);
        OrderInventorySnapshot inventorySnapshot = orderRepository
                .findInventorySnapshotByOrderNo(order.getOrderNo() == null ? null : order.getOrderNo().value())
                .orElse(null);
        List<OrderItemDTO> itemDtos =
                orderRepository
                        .findItemsByOrderId(order.getId() == null ? null : OrderIdCodec.toValue(order.getId()))
                        .stream()
                        .map(item -> new OrderItemDTO(
                                item.getSkuId() == null ? null : item.getSkuId().value(),
                                item.getSkuName(),
                                item.getImageUrl(),
                                item.getQuantity(),
                                item.getSalePrice().value(),
                                item.getLineAmount().value()))
                        .toList();
        return new OrderDetailDTO(
                order.getId() == null ? null : OrderIdCodec.toValue(order.getId()),
                tenantId,
                order.getOrderNo() == null ? null : order.getOrderNo().value(),
                order.getUserId() == null ? null : order.getUserId().value(),
                order.getOrderStatus() == null ? null : order.getOrderStatus().value(),
                order.getPayStatus() == null ? null : order.getPayStatus().value(),
                order.getInventoryStatus() == null ? null : order.getInventoryStatus().value(),
                paymentSnapshot == null
                        ? (order.getPaymentNo() == null ? null : order.getPaymentNo().value())
                        : (paymentSnapshot.getPaymentNo() == null ? null : paymentSnapshot.getPaymentNo().value()),
                inventorySnapshot == null
                        ? (order.getReservationNo() == null ? null : ReservationNoCodec.toValue(order.getReservationNo()))
                        : (inventorySnapshot.getReservationNo() == null
                                ? null
                                : ReservationNoCodec.toValue(inventorySnapshot.getReservationNo())),
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
                ? (order.getPaymentNo() == null ? null : order.getPaymentNo().value())
                : (paymentSnapshot.getPaymentNo() == null ? null : paymentSnapshot.getPaymentNo().value());
        String payStatus = paymentSnapshot == null
                ? (order.getPayStatus() == null ? null : order.getPayStatus().value())
                : (paymentSnapshot.getPayStatus() == null ? null : paymentSnapshot.getPayStatus().value());
        String channelCode = paymentSnapshot == null
                ? (order.getPaymentChannelCode() == null ? null : order.getPaymentChannelCode().value())
                : (paymentSnapshot.getChannelCode() == null ? null : paymentSnapshot.getChannelCode().value());
        BigDecimal paidAmount = paymentSnapshot == null
                ? (order.getPaidAmount() == null ? null : order.getPaidAmount().value())
                : (paymentSnapshot.getPaidAmount() == null ? null : paymentSnapshot.getPaidAmount().value());
        String channelStatus = paymentSnapshot == null
                ? order.getPaymentChannelStatus()
                : (paymentSnapshot.getChannelStatus() == null ? null : paymentSnapshot.getChannelStatus().value());
        String failureReason =
                paymentSnapshot == null ? order.getPaymentFailureReason() : paymentSnapshot.getFailureReason();
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
                        ? (order.getReservationNo() == null ? null : ReservationNoCodec.toValue(order.getReservationNo()))
                        : (inventorySnapshot.getReservationNo() == null
                                ? null
                                : ReservationNoCodec.toValue(inventorySnapshot.getReservationNo())),
                "N/A");
        String inventoryStatus = inventorySnapshot == null
                ? (order.getInventoryStatus() == null ? null : order.getInventoryStatus().value())
                : (inventorySnapshot.getInventoryStatus() == null ? null : inventorySnapshot.getInventoryStatus().value());
        String warehouseCode = inventorySnapshot == null
                ? (order.getWarehouseCode() == null ? null : order.getWarehouseCode().value())
                : (inventorySnapshot.getWarehouseCode() == null ? null : inventorySnapshot.getWarehouseCode().value());
        String failureReason =
                inventorySnapshot == null ? order.getInventoryFailureReason() : inventorySnapshot.getFailureReason();
        return "reservationNo=" + reservationNo
                + ",inventoryStatus=" + inventoryStatus
                + ",warehouseCode=" + Objects.toString(warehouseCode, "N/A")
                + ",failureReason=" + Objects.toString(failureReason, "N/A");
    }
}
