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

    private static final String OPERATOR_TYPE_SYSTEM = "SYSTEM";
    private static final String OPERATOR_ID_SYSTEM = "0";

    private final OrderRepository orderRepository;

    public OrderDerivedDataPersistenceSupport(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void persist(Order order, String actionType, String beforeStatus) {
        Instant now = Instant.now();
        if (order.getPaymentNoValue() != null && !order.getPaymentNoValue().isBlank()) {
            orderRepository.savePaymentSnapshot(new OrderPaymentSnapshot(null, order.getTenantIdValue(), toOrderIdValue(order),
                    order.getPaymentNoValue(), order.getPaymentChannelCode(), order.getPayStatus(),
                    order.getPaidAmount() == null ? null : order.getPaidAmount().value(),
                    order.getPaidAt(), order.getPaymentFailureReason(), order.getPaymentChannelStatus(), now));
        }
        if (order.getReservationNoValue() != null && !order.getReservationNoValue().isBlank()) {
            orderRepository.saveInventorySnapshot(new OrderInventorySnapshot(null, order.getTenantIdValue(), toOrderIdValue(order),
                    order.getReservationNoValue(), order.getInventoryStatus(), order.getWarehouseNo(),
                    order.getInventoryFailureReason(), now));
        }
        orderRepository.saveAuditLog(new OrderAuditLog(null, order.getTenantId(), order.getOrderNoValue(), actionType,
                beforeStatus, order.getOrderStatus(), OPERATOR_TYPE_SYSTEM, OPERATOR_ID_SYSTEM, now));
    }

    private Long toOrderIdValue(Order order) {
        return order.getId() == null ? null : Long.valueOf(order.getId().value());
    }
}
