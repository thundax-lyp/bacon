package com.github.thundax.bacon.order.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannel;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderPaymentSnapshotDO;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentSnapshotPersistenceAssembler {

    public OrderPaymentSnapshotDO toDataObject(OrderPaymentSnapshot snapshot) {
        return new OrderPaymentSnapshotDO(
                snapshot.getId(),
                BaconContextHolder.requireTenantId(),
                snapshot.getOrderId() == null ? null : snapshot.getOrderId().value(),
                snapshot.getPaymentNo() == null ? null : snapshot.getPaymentNo().value(),
                snapshot.getChannelCode() == null
                        ? null
                        : snapshot.getChannelCode().value(),
                snapshot.getPayStatus() == null ? null : snapshot.getPayStatus().value(),
                snapshot.getPaidAmount() == null
                        ? null
                        : snapshot.getPaidAmount().currencyCode().value(),
                snapshot.getPaidAmount() == null
                        ? null
                        : snapshot.getPaidAmount().value(),
                snapshot.getPaidTime(),
                snapshot.getFailureReason(),
                snapshot.getChannelStatus() == null
                        ? null
                        : snapshot.getChannelStatus().value(),
                snapshot.getUpdatedAt());
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
