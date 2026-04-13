package com.github.thundax.bacon.order.application.support;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.enums.OperatorType;
import com.github.thundax.bacon.order.domain.model.enums.OrderAuditActionType;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class OrderDerivedDataPersistenceSupport {

    private static final String OPERATOR_ID_SYSTEM = "0";

    private final OrderRepository orderRepository;

    public OrderDerivedDataPersistenceSupport(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void persist(Order order, OrderAuditActionType actionType, OrderStatus beforeStatus) {
        Instant now = Instant.now();
        if (order.getPaymentNo() != null && !order.getPaymentNo().value().isBlank()) {
            orderRepository.savePaymentSnapshot(OrderPaymentSnapshot.create(
                    order.getId(),
                    order.getPaymentNo(),
                    order.getPaymentChannelCode(),
                    order.getPayStatus(),
                    toMoney(order.getPaidAmount(), order.getCurrencyCode()),
                    order.getPaidAt(),
                    order.getPaymentFailureReason(),
                    toPaymentChannelStatus(order.getPaymentChannelStatus()),
                    now));
        }
        if (order.getReservationNo() != null
                && !order.getReservationNo().value().isBlank()) {
            orderRepository.saveInventorySnapshot(OrderInventorySnapshot.create(
                    order.getOrderNo(),
                    order.getReservationNo(),
                    order.getInventoryStatus(),
                    order.getWarehouseCode(),
                    order.getInventoryFailureReason(),
                    now));
        }
        orderRepository.saveAuditLog(OrderAuditLog.create(
                order.getOrderNo(),
                actionType,
                beforeStatus,
                order.getOrderStatus(),
                OperatorType.SYSTEM,
                OPERATOR_ID_SYSTEM,
                now));
    }

    private PaymentChannelStatus toPaymentChannelStatus(String channelStatus) {
        return channelStatus == null || channelStatus.isBlank() ? null : PaymentChannelStatus.from(channelStatus);
    }

    private Money toMoney(Money paidAmount, CurrencyCode currencyCode) {
        return paidAmount == null || currencyCode == null ? null : Money.of(paidAmount.value(), currencyCode);
    }
}
