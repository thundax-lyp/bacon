package com.github.thundax.bacon.order.application.query;

import com.github.thundax.bacon.common.commerce.valueobject.Money;
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

    public OrderDetailDTO getById(Long tenantId, Long orderId) {
        OrderDetailDTO dto = orderRepository
                .findById(orderId)
                .map(this::toDetail)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (tenantId != null && !tenantId.equals(dto.getTenantId())) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        return dto;
    }

    public OrderDetailDTO getByOrderNo(Long tenantId, String orderNo) {
        return orderRepository
                .findByOrderNo(tenantId, orderNo)
                .map(this::toDetail)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
    }

    public OrderPageResultDTO pageOrders(OrderPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        long total = orderRepository.countOrders(
                query.getTenantId(),
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
                        query.getTenantId(),
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
        return new OrderSummaryDTO(
                valueOf(order.getId()),
                valueOf(order.getTenantId()),
                valueOf(order.getOrderNo()),
                order.getUserId() == null ? null : order.getUserId().value(),
                valueOf(order.getOrderStatus()),
                valueOf(order.getPayStatus()),
                valueOf(order.getInventoryStatus()),
                valueOf(order.getPaymentNo()),
                valueOf(order.getReservationNo()),
                valueOf(order.getCurrencyCode()),
                order.getTotalAmount().value(),
                order.getPayableAmount().value(),
                order.getCancelReason(),
                order.getCloseReason(),
                order.getCreatedAt(),
                order.getExpiredAt());
    }

    private OrderDetailDTO toDetail(Order order) {
        OrderPaymentSnapshot paymentSnapshot = orderRepository
                .findPaymentSnapshotByOrderId(valueOf(order.getTenantId()), valueOf(order.getId()), valueOf(order.getCurrencyCode()))
                .orElse(null);
        OrderInventorySnapshot inventorySnapshot = orderRepository
                .findInventorySnapshotByOrderNo(valueOf(order.getTenantId()), valueOf(order.getOrderNo()))
                .orElse(null);
        List<OrderItemDTO> itemDtos =
                orderRepository
                        .findItemsByOrderId(valueOf(order.getTenantId()), valueOf(order.getId()), valueOf(order.getCurrencyCode()))
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
                valueOf(order.getId()),
                valueOf(order.getTenantId()),
                valueOf(order.getOrderNo()),
                order.getUserId() == null ? null : order.getUserId().value(),
                valueOf(order.getOrderStatus()),
                valueOf(order.getPayStatus()),
                valueOf(order.getInventoryStatus()),
                paymentSnapshot == null ? valueOf(order.getPaymentNo()) : valueOf(paymentSnapshot.getPaymentNo()),
                inventorySnapshot == null ? valueOf(order.getReservationNo()) : valueOf(inventorySnapshot.getReservationNo()),
                valueOf(order.getCurrencyCode()),
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
        String paymentNo = paymentSnapshot == null ? valueOf(order.getPaymentNo()) : valueOf(paymentSnapshot.getPaymentNo());
        String payStatus = paymentSnapshot == null ? valueOf(order.getPayStatus()) : valueOf(paymentSnapshot.getPayStatus());
        String channelCode = paymentSnapshot == null
                ? valueOf(order.getPaymentChannelCode())
                : valueOf(paymentSnapshot.getChannelCode());
        BigDecimal paidAmount = paymentSnapshot == null
                ? toAmountValue(order.getPaidAmount())
                : toAmountValue(paymentSnapshot.getPaidAmount());
        String channelStatus =
                paymentSnapshot == null ? order.getPaymentChannelStatus() : valueOf(paymentSnapshot.getChannelStatus());
        String failureReason =
                paymentSnapshot == null ? order.getPaymentFailureReason() : paymentSnapshot.getFailureReason();
        return "paymentNo=" + paymentNo
                + ",payStatus=" + payStatus
                + ",channelCode=" + Objects.toString(channelCode, "N/A")
                + ",paidAmount=" + Objects.toString(paidAmount, "N/A")
                + ",channelStatus=" + Objects.toString(channelStatus, "N/A")
                + ",failureReason=" + Objects.toString(failureReason, "N/A");
    }

    private BigDecimal toAmountValue(Money money) {
        return money == null ? null : money.value();
    }

    private String buildInventorySnapshot(Order order, OrderInventorySnapshot inventorySnapshot) {
        String reservationNo = Objects.toString(
                inventorySnapshot == null ? valueOf(order.getReservationNo()) : valueOf(inventorySnapshot.getReservationNo()),
                "N/A");
        String inventoryStatus =
                inventorySnapshot == null ? valueOf(order.getInventoryStatus()) : valueOf(inventorySnapshot.getInventoryStatus());
        String warehouseCode =
                inventorySnapshot == null ? valueOf(order.getWarehouseCode()) : valueOf(inventorySnapshot.getWarehouseCode());
        String failureReason =
                inventorySnapshot == null ? order.getInventoryFailureReason() : inventorySnapshot.getFailureReason();
        return "reservationNo=" + reservationNo
                + ",inventoryStatus=" + inventoryStatus
                + ",warehouseCode=" + Objects.toString(warehouseCode, "N/A")
                + ",failureReason=" + Objects.toString(failureReason, "N/A");
    }

    private Long valueOf(com.github.thundax.bacon.order.domain.model.valueobject.OrderId orderId) {
        return OrderIdCodec.toValue(orderId);
    }

    private Long valueOf(com.github.thundax.bacon.common.id.domain.TenantId tenantId) {
        return tenantId == null ? null : tenantId.value();
    }

    private String valueOf(com.github.thundax.bacon.common.commerce.valueobject.OrderNo orderNo) {
        return orderNo == null ? null : orderNo.value();
    }

    private String valueOf(com.github.thundax.bacon.common.commerce.valueobject.PaymentNo paymentNo) {
        return paymentNo == null ? null : paymentNo.value();
    }

    private String valueOf(com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo reservationNo) {
        return ReservationNoCodec.toValue(reservationNo);
    }

    private String valueOf(com.github.thundax.bacon.common.commerce.enums.CurrencyCode currencyCode) {
        return currencyCode == null ? null : currencyCode.value();
    }

    private String valueOf(com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode warehouseCode) {
        return warehouseCode == null ? null : warehouseCode.value();
    }

    private String valueOf(com.github.thundax.bacon.order.domain.model.enums.OrderStatus orderStatus) {
        return orderStatus == null ? null : orderStatus.value();
    }

    private String valueOf(com.github.thundax.bacon.order.domain.model.enums.PayStatus payStatus) {
        return payStatus == null ? null : payStatus.value();
    }

    private String valueOf(com.github.thundax.bacon.order.domain.model.enums.InventoryStatus inventoryStatus) {
        return inventoryStatus == null ? null : inventoryStatus.value();
    }

    private String valueOf(com.github.thundax.bacon.order.domain.model.enums.PaymentChannel paymentChannel) {
        return paymentChannel == null ? null : paymentChannel.value();
    }

    private String valueOf(com.github.thundax.bacon.order.domain.model.enums.PaymentChannelStatus channelStatus) {
        return channelStatus == null ? null : channelStatus.value();
    }
}
