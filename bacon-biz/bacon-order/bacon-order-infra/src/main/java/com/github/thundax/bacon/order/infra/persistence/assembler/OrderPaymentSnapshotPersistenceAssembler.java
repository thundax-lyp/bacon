package com.github.thundax.bacon.order.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannel;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderPaymentSnapshotDO;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentSnapshotPersistenceAssembler {

    public OrderPaymentSnapshotDO toDataObject(OrderPaymentSnapshot snapshot) {
        return new OrderPaymentSnapshotDO(
                snapshot.id(),
                BaconContextHolder.requireTenantId(),
                snapshot.orderId() == null ? null : snapshot.orderId().value(),
                snapshot.paymentNo() == null ? null : snapshot.paymentNo().value(),
                snapshot.channelCode() == null
                        ? null
                        : snapshot.channelCode().value(),
                snapshot.payStatus() == null ? null : snapshot.payStatus().value(),
                snapshot.paidAmount() == null
                        ? null
                        : snapshot.paidAmount().currencyCode().value(),
                snapshot.paidAmount() == null
                        ? null
                        : snapshot.paidAmount().value(),
                snapshot.paidTime(),
                snapshot.failureReason(),
                snapshot.channelStatus() == null
                        ? null
                        : snapshot.channelStatus().value(),
                snapshot.updatedAt());
    }

    public OrderPaymentSnapshot toDomain(OrderPaymentSnapshotDO dataObject) {
        return OrderPaymentSnapshot.reconstruct(
                dataObject.getId(),
                dataObject.getOrderId() == null ? null : OrderId.of(dataObject.getOrderId()),
                dataObject.getPaymentNo() == null ? null : PaymentNo.of(dataObject.getPaymentNo()),
                dataObject.getChannelCode() == null ? null : PaymentChannel.from(dataObject.getChannelCode()),
                dataObject.getPayStatus() == null ? null : PayStatus.from(dataObject.getPayStatus()),
                toMoney(dataObject.getPaidAmount(), dataObject.getCurrencyCode()),
                dataObject.getPaidTime(),
                dataObject.getFailureReason(),
                dataObject.getChannelStatus() == null ? null : PaymentChannelStatus.from(dataObject.getChannelStatus()),
                dataObject.getUpdatedAt());
    }

    private Money toMoney(BigDecimal value, String currencyCode) {
        if (value == null || currencyCode == null || currencyCode.isBlank()) {
            return null;
        }
        return Money.of(value, CurrencyCode.fromValue(currencyCode));
    }
}
