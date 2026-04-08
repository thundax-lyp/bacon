package com.github.thundax.bacon.order.application.query;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderItemDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
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
        OrderDetailDTO dto = orderRepository.findById(orderId)
                .map(this::toDetail)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        if (tenantId != null && !tenantId.equals(dto.getTenantId())) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }
        return dto;
    }

    public OrderDetailDTO getByOrderNo(Long tenantId, String orderNo) {
        return orderRepository.findByOrderNo(tenantId, orderNo)
                .map(this::toDetail)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderNo));
    }

    public OrderPageResultDTO pageOrders(OrderPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        long total = orderRepository.countOrders(query.getTenantId(), query.getUserId(), query.getOrderNo(),
                query.getOrderStatus(), query.getPayStatus(), query.getInventoryStatus(),
                query.getCreatedAtFrom(), query.getCreatedAtTo());
        List<Order> pageOrders = total <= 0 ? List.of() : orderRepository.pageOrders(query.getTenantId(),
                query.getUserId(), query.getOrderNo(), query.getOrderStatus(), query.getPayStatus(),
                query.getInventoryStatus(), query.getCreatedAtFrom(), query.getCreatedAtTo(), offset, pageSize);
        List<OrderSummaryDTO> records = pageOrders.stream()
                .map(this::toSummary)
                .toList();
        return new OrderPageResultDTO(records, total, pageNo, pageSize);
    }

    private OrderSummaryDTO toSummary(Order order) {
        return new OrderSummaryDTO(order.getIdValue(), order.getTenantIdValue(), order.getOrderNoValue(),
                order.getUserId() == null ? null : order.getUserId().value(),
                order.getOrderStatusValue(), order.getPayStatusValue(), order.getInventoryStatusValue(), order.getPaymentNoValue(),
                order.getReservationNoValue(), order.getCurrencyCodeValue(), order.getTotalAmount().value(),
                order.getPayableAmount().value(),
                order.getCancelReason(), order.getCloseReason(), order.getCreatedAt(), order.getExpiredAt());
    }

    private OrderDetailDTO toDetail(Order order) {
        OrderPaymentSnapshot paymentSnapshot = orderRepository.findPaymentSnapshotByOrderId(order.getTenantIdValue(),
                order.getIdValue(), order.getCurrencyCodeValue()).orElse(null);
        OrderInventorySnapshot inventorySnapshot = orderRepository.findInventorySnapshotByOrderNo(order.getTenantIdValue(),
                order.getOrderNoValue()).orElse(null);
        List<OrderItemDTO> itemDtos = orderRepository.findItemsByOrderId(order.getTenantIdValue(), order.getIdValue(),
                        order.getCurrencyCodeValue()).stream()
                .map(item -> new OrderItemDTO(item.getSkuId() == null ? null : item.getSkuId().value(),
                        item.getSkuName(), item.getImageUrl(), item.getQuantity(),
                        item.getSalePrice().value(), item.getLineAmount().value()))
                .toList();
        return new OrderDetailDTO(order.getIdValue(), order.getTenantIdValue(), order.getOrderNoValue(),
                order.getUserId() == null ? null : order.getUserId().value(),
                order.getOrderStatusValue(), order.getPayStatusValue(), order.getInventoryStatusValue(),
                paymentSnapshot == null ? order.getPaymentNoValue() : paymentSnapshot.paymentNoValue(),
                inventorySnapshot == null ? order.getReservationNoValue() : inventorySnapshot.reservationNoValue(),
                order.getCurrencyCodeValue(), order.getTotalAmount().value(), order.getPayableAmount().value(), order.getCancelReason(),
                order.getCloseReason(), order.getCreatedAt(), order.getExpiredAt(), itemDtos,
                buildPaymentSnapshot(order, paymentSnapshot), buildInventorySnapshot(order, inventorySnapshot),
                order.getPaidAt(), order.getClosedAt());
    }

    private String buildPaymentSnapshot(Order order, OrderPaymentSnapshot paymentSnapshot) {
        if (paymentSnapshot == null && order.getPaymentNoValue() == null) {
            return null;
        }
        String paymentNo = paymentSnapshot == null ? order.getPaymentNoValue() : paymentSnapshot.paymentNoValue();
        String payStatus = paymentSnapshot == null ? order.getPayStatusValue() : paymentSnapshot.payStatusValue();
        String channelCode = paymentSnapshot == null ? order.getPaymentChannelCodeValue() : paymentSnapshot.channelCodeValue();
        BigDecimal paidAmount = paymentSnapshot == null ? toAmountValue(order.getPaidAmount()) : toAmountValue(paymentSnapshot.paidAmount());
        String channelStatus = paymentSnapshot == null
                ? order.getPaymentChannelStatus() : paymentSnapshot.channelStatusValue();
        String failureReason = paymentSnapshot == null
                ? order.getPaymentFailureReason() : paymentSnapshot.failureReason();
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
                inventorySnapshot == null ? order.getReservationNoValue() : inventorySnapshot.reservationNoValue(), "N/A");
        String inventoryStatus = inventorySnapshot == null ? order.getInventoryStatusValue() : inventorySnapshot.inventoryStatusValue();
        String warehouseNo = inventorySnapshot == null ? order.getWarehouseNoValue() : inventorySnapshot.warehouseNoValue();
        String failureReason = inventorySnapshot == null
                ? order.getInventoryFailureReason() : inventorySnapshot.failureReason();
        return "reservationNo=" + reservationNo
                + ",inventoryStatus=" + inventoryStatus
                + ",warehouseNo=" + Objects.toString(warehouseNo, "N/A")
                + ",failureReason=" + Objects.toString(failureReason, "N/A");
    }
}
