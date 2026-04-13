package com.github.thundax.bacon.payment.infra.persistence.assembler;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelCode;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.payment.infra.persistence.dataobject.PaymentCallbackRecordDO;
import org.springframework.stereotype.Component;

@Component
public class PaymentCallbackRecordPersistenceAssembler {

    public PaymentCallbackRecordDO toDataObject(PaymentCallbackRecord callbackRecord) {
        return new PaymentCallbackRecordDO(
                callbackRecord.getId(),
                BaconContextHolder.requireTenantId(),
                callbackRecord.getPaymentNo() == null
                        ? null
                        : callbackRecord.getPaymentNo().value(),
                callbackRecord.getOrderNo() == null
                        ? null
                        : callbackRecord.getOrderNo().value(),
                callbackRecord.getChannelCode() == null
                        ? null
                        : callbackRecord.getChannelCode().value(),
                callbackRecord.getChannelTransactionNo(),
                callbackRecord.getChannelStatus() == null
                        ? null
                        : callbackRecord.getChannelStatus().value(),
                callbackRecord.getRawPayload(),
                callbackRecord.getReceivedAt());
    }

    public PaymentCallbackRecord toDomain(PaymentCallbackRecordDO dataObject) {
        return PaymentCallbackRecord.reconstruct(
                dataObject.getId(),
                dataObject.getPaymentNo() == null ? null : PaymentNo.of(dataObject.getPaymentNo()),
                dataObject.getOrderNo() == null ? null : OrderNo.of(dataObject.getOrderNo()),
                dataObject.getChannelCode() == null ? null : PaymentChannelCode.fromValue(dataObject.getChannelCode()),
                dataObject.getChannelTransactionNo(),
                dataObject.getChannelStatus() == null
                        ? null
                        : PaymentChannelStatus.fromValue(dataObject.getChannelStatus()),
                dataObject.getRawPayload(),
                dataObject.getReceivedAt());
    }
}
