package com.github.thundax.bacon.payment.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelCode;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.domain.model.valueobject.PaymentOrderId;
import com.github.thundax.bacon.payment.infra.persistence.dataobject.PaymentOrderDO;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class PaymentOrderPersistenceAssembler {

    public PaymentOrderDO toDataObject(PaymentOrder paymentOrder, Instant updatedAt) {
        return new PaymentOrderDO(
                paymentOrder.getId() == null ? null : paymentOrder.getId().value(),
                BaconContextHolder.requireTenantId(),
                paymentOrder.getPaymentNo() == null ? null : paymentOrder.getPaymentNo().value(),
                paymentOrder.getOrderNo() == null ? null : paymentOrder.getOrderNo().value(),
                paymentOrder.getUserId() == null ? null : paymentOrder.getUserId().value(),
                paymentOrder.getChannelCode() == null ? null : paymentOrder.getChannelCode().value(),
                paymentOrder.getPaymentStatus() == null ? null : paymentOrder.getPaymentStatus().value(),
                paymentOrder.getAmount() == null ? null : paymentOrder.getAmount().value(),
                paymentOrder.getPaidAmount() == null ? null : paymentOrder.getPaidAmount().value(),
                paymentOrder.getSubject(),
                paymentOrder.getCreatedAt(),
                updatedAt,
                paymentOrder.getExpiredAt(),
                paymentOrder.getPaidAt(),
                paymentOrder.getClosedAt());
    }

    public PaymentOrder toDomain(PaymentOrderDO dataObject) {
        return PaymentOrder.reconstruct(
                dataObject.getId() == null ? null : PaymentOrderId.of(dataObject.getId()),
                dataObject.getPaymentNo() == null ? null : PaymentNo.of(dataObject.getPaymentNo()),
                dataObject.getOrderNo() == null ? null : OrderNo.of(dataObject.getOrderNo()),
                dataObject.getUserId() == null ? null : UserId.of(dataObject.getUserId()),
                dataObject.getChannelCode() == null ? null : PaymentChannelCode.fromValue(dataObject.getChannelCode()),
                dataObject.getAmount() == null ? null : Money.of(dataObject.getAmount()),
                dataObject.getPaidAmount() == null ? Money.zero() : Money.of(dataObject.getPaidAmount()),
                dataObject.getSubject(),
                dataObject.getCreatedAt(),
                dataObject.getExpiredAt(),
                dataObject.getPaidAt(),
                dataObject.getClosedAt(),
                dataObject.getPaymentStatus() == null ? null : PaymentStatus.fromValue(dataObject.getPaymentStatus()),
                null,
                null,
                null);
    }
}
