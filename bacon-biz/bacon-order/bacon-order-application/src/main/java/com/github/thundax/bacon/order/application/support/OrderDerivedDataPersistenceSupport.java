package com.github.thundax.bacon.order.application.support;

import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class OrderDerivedDataPersistenceSupport {

    private final OrderRepository orderRepository;

    public OrderDerivedDataPersistenceSupport(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void persist(Order order, String actionType, String beforeStatus) {
        Instant now = Instant.now();
        if (order.getPaymentNo() != null && !order.getPaymentNo().isBlank()) {
            orderRepository.savePaymentSnapshot(new OrderPaymentSnapshot(null, order.getTenantId(), order.getId(),
                    order.getPaymentNo(), order.getPaymentChannelCode(), order.getPayStatus(), order.getPaidAmount(),
                    order.getPaidAt(), order.getPaymentFailureReason(), order.getPaymentChannelStatus(), now));
        }
        if (order.getReservationNo() != null && !order.getReservationNo().isBlank()) {
            orderRepository.saveInventorySnapshot(new OrderInventorySnapshot(null, order.getTenantId(), order.getId(),
                    order.getReservationNo(), order.getInventoryStatus(), order.getWarehouseId(),
                    order.getInventoryFailureReason(), now));
        }
        orderRepository.saveAuditLog(new OrderAuditLog(null, order.getTenantId(), order.getOrderNo(), actionType,
                beforeStatus, order.getOrderStatus(), OrderAuditLog.OPERATOR_TYPE_SYSTEM,
                OrderAuditLog.OPERATOR_ID_SYSTEM, now));
    }
}
