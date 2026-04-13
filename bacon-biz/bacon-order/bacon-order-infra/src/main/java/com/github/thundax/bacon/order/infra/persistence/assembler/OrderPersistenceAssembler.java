package com.github.thundax.bacon.order.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannel;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderDO;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderInventorySnapshotDO;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderPaymentSnapshotDO;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class OrderPersistenceAssembler {

    public OrderDO toDataObject(Order order) {
        return new OrderDO(
                order.getId() == null ? null : order.getId().value(),
                BaconContextHolder.requireTenantId(),
                order.getOrderNo() == null ? null : order.getOrderNo().value(),
                order.getUserId() == null ? null : order.getUserId().value(),
                order.getOrderStatus() == null ? null : order.getOrderStatus().value(),
                order.getPayStatus() == null ? null : order.getPayStatus().value(),
                order.getInventoryStatus() == null
                        ? null
                        : order.getInventoryStatus().value(),
                order.getCurrencyCode() == null ? null : order.getCurrencyCode().value(),
                order.getTotalAmount() == null ? null : order.getTotalAmount().value(),
                order.getPayableAmount() == null
                        ? null
                        : order.getPayableAmount().value(),
                order.getRemark(),
                order.getCancelReason(),
                order.getCloseReason(),
                order.getCreatedAt(),
                null,
                order.getExpiredAt(),
                order.getPaidAt(),
                order.getClosedAt());
    }

    public Order toDomain(
            OrderDO dataObject, OrderPaymentSnapshotDO paymentSnapshot, OrderInventorySnapshotDO inventorySnapshot) {
        String currencyCode = dataObject.getCurrencyCode();
        return Order.reconstruct(
                dataObject.getId() == null ? null : OrderId.of(dataObject.getId()),
                dataObject.getOrderNo() == null ? null : OrderNo.of(dataObject.getOrderNo()),
                dataObject.getUserId() == null ? null : UserId.of(dataObject.getUserId()),
                dataObject.getOrderStatus() == null ? null : OrderStatus.from(dataObject.getOrderStatus()),
                dataObject.getPayStatus() == null ? null : PayStatus.from(dataObject.getPayStatus()),
                dataObject.getInventoryStatus() == null ? null : InventoryStatus.from(dataObject.getInventoryStatus()),
                paymentSnapshot == null || paymentSnapshot.getPaymentNo() == null
                        ? null
                        : PaymentNo.of(paymentSnapshot.getPaymentNo()),
                inventorySnapshot == null || inventorySnapshot.getReservationNo() == null
                        ? null
                        : ReservationNo.of(inventorySnapshot.getReservationNo()),
                currencyCode == null ? null : CurrencyCode.fromValue(currencyCode),
                toMoney(dataObject.getTotalAmount(), currencyCode),
                toMoney(dataObject.getPayableAmount(), currencyCode),
                dataObject.getRemark(),
                dataObject.getCancelReason(),
                dataObject.getCloseReason(),
                dataObject.getCreatedAt(),
                dataObject.getExpiredAt(),
                dataObject.getPaidAt(),
                dataObject.getClosedAt(),
                paymentSnapshot == null || paymentSnapshot.getChannelCode() == null
                        ? null
                        : PaymentChannel.from(paymentSnapshot.getChannelCode()),
                paymentSnapshot == null ? null : toMoney(paymentSnapshot.getPaidAmount(), currencyCode),
                paymentSnapshot == null ? null : paymentSnapshot.getChannelStatus(),
                paymentSnapshot == null ? null : paymentSnapshot.getFailureReason(),
                null,
                inventorySnapshot == null || inventorySnapshot.getWarehouseCode() == null
                        ? null
                        : WarehouseCode.of(inventorySnapshot.getWarehouseCode()),
                inventorySnapshot == null ? null : inventorySnapshot.getFailureReason(),
                null,
                null,
                null);
    }

    private Money toMoney(BigDecimal value, String currencyCode) {
        if (value == null || currencyCode == null || currencyCode.isBlank()) {
            return null;
        }
        return Money.of(value, CurrencyCode.fromValue(currencyCode));
    }
}
