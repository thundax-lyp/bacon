package com.github.thundax.bacon.order.application.support;

import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.enums.OperatorType;
import com.github.thundax.bacon.order.domain.model.enums.OrderAuditActionType;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.repository.OrderAuditLogRepository;
import com.github.thundax.bacon.order.domain.repository.OrderInventorySnapshotRepository;
import com.github.thundax.bacon.order.domain.repository.OrderPaymentSnapshotRepository;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class OrderDerivedDataPersistenceSupport {

    private static final String OPERATOR_ID_SYSTEM = "0";
    private static final String INVENTORY_SNAPSHOT_ID_BIZ_TAG = "order_inventory_snapshot_id";

    private final OrderAuditLogRepository orderAuditLogRepository;
    private final OrderRepository orderRepository;
    private final OrderInventorySnapshotRepository orderInventorySnapshotRepository;
    private final OrderPaymentSnapshotRepository orderPaymentSnapshotRepository;
    private final IdGenerator idGenerator;

    public OrderDerivedDataPersistenceSupport(
            OrderAuditLogRepository orderAuditLogRepository,
            OrderRepository orderRepository,
            OrderInventorySnapshotRepository orderInventorySnapshotRepository,
            OrderPaymentSnapshotRepository orderPaymentSnapshotRepository,
            IdGenerator idGenerator) {
        this.orderAuditLogRepository = orderAuditLogRepository;
        this.orderRepository = orderRepository;
        this.orderInventorySnapshotRepository = orderInventorySnapshotRepository;
        this.orderPaymentSnapshotRepository = orderPaymentSnapshotRepository;
        this.idGenerator = idGenerator;
    }

    public void persist(Order order, OrderAuditActionType actionType, OrderStatus beforeStatus) {
        Instant now = Instant.now();
        if (order.getPaymentNo() != null && !order.getPaymentNo().value().isBlank()) {
            OrderPaymentSnapshot paymentSnapshot = OrderPaymentSnapshot.create(
                    order.getId(),
                    order.getPaymentNo(),
                    order.getPaymentChannelCode(),
                    order.getPayStatus(),
                    order.getPaidAmount() == null || order.getCurrencyCode() == null
                            ? null
                            : Money.of(order.getPaidAmount().value(), order.getCurrencyCode()),
                    order.getPaidAt(),
                    order.getPaymentFailureReason(),
                    order.getPaymentChannelStatus() == null
                                    || order.getPaymentChannelStatus().isBlank()
                            ? null
                            : PaymentChannelStatus.from(order.getPaymentChannelStatus()),
                    now);
            if (orderPaymentSnapshotRepository.findByOrderId(order.getId()).isPresent()) {
                orderPaymentSnapshotRepository.update(paymentSnapshot);
            } else {
                orderPaymentSnapshotRepository.insert(paymentSnapshot);
            }
        }
        if (order.getReservationNo() != null
                && !order.getReservationNo().value().isBlank()) {
            OrderInventorySnapshot inventorySnapshot = OrderInventorySnapshot.create(
                    idGenerator.nextId(INVENTORY_SNAPSHOT_ID_BIZ_TAG),
                    order.getOrderNo(),
                    order.getReservationNo(),
                    order.getInventoryStatus(),
                    order.getWarehouseCode(),
                    order.getInventoryFailureReason(),
                    now);
            if (orderInventorySnapshotRepository.findByOrderNo(order.getOrderNo()).isPresent()) {
                orderInventorySnapshotRepository.update(inventorySnapshot);
            } else {
                orderInventorySnapshotRepository.insert(inventorySnapshot);
            }
        }
        orderAuditLogRepository.insert(OrderAuditLog.create(
                order.getOrderNo(),
                actionType,
                beforeStatus,
                order.getOrderStatus(),
                OperatorType.SYSTEM,
                OPERATOR_ID_SYSTEM,
                now));
    }
}
