package com.github.thundax.bacon.order.application.support;

import com.github.thundax.bacon.common.core.enums.CurrencyCode;
import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.enums.OperatorType;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannel;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderAuditActionType;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.order.domain.model.valueobject.PaymentNo;
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
        if (order.getPaymentNoValue() != null && !order.getPaymentNoValue().isBlank()) {
            orderRepository.savePaymentSnapshot(new OrderPaymentSnapshot(null, order.getTenantId(), toOrderId(order),
                    toPaymentNo(order.getPaymentNoValue()), toPaymentChannel(order.getPaymentChannelCode()),
                    toPayStatus(order.getPayStatus()),
                    toMoney(order.getPaidAmount(), order.getCurrencyCodeValue()),
                    order.getPaidAt(), order.getPaymentFailureReason(),
                    toPaymentChannelStatus(order.getPaymentChannelStatus()), now));
        }
        if (order.getReservationNoValue() != null && !order.getReservationNoValue().isBlank()) {
            orderRepository.saveInventorySnapshot(new OrderInventorySnapshot(order.getTenantId(), order.getOrderNo(),
                    order.getReservationNo(), order.getInventoryStatusEnum(), order.getWarehouseNo(),
                    order.getInventoryFailureReason(), now));
        }
        orderRepository.saveAuditLog(new OrderAuditLog(null, order.getTenantId(), order.getOrderNo(), actionType,
                beforeStatus, order.getOrderStatusEnum(), OperatorType.SYSTEM, OPERATOR_ID_SYSTEM, now));
    }

    private OrderId toOrderId(Order order) {
        return order.getId();
    }

    private PaymentNo toPaymentNo(String paymentNo) {
        return paymentNo == null ? null : PaymentNo.of(paymentNo);
    }

    private PaymentChannel toPaymentChannel(String channelCode) {
        return channelCode == null || channelCode.isBlank() ? null : PaymentChannel.fromValue(channelCode);
    }

    private PayStatus toPayStatus(String payStatus) {
        return payStatus == null || payStatus.isBlank() ? null : PayStatus.fromValue(payStatus);
    }

    private PaymentChannelStatus toPaymentChannelStatus(String channelStatus) {
        return channelStatus == null || channelStatus.isBlank() ? null : PaymentChannelStatus.fromValue(channelStatus);
    }

    private Money toMoney(Money paidAmount, String currencyCode) {
        return paidAmount == null ? null : Money.of(paidAmount.value(), CurrencyCode.fromValue(currencyCode));
    }
}
