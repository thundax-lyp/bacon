package com.github.thundax.bacon.payment.application.assembler;

import com.github.thundax.bacon.payment.application.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;

public final class PaymentOrderAssembler {

    private PaymentOrderAssembler() {}

    public static PaymentDetailDTO toDetail(PaymentOrder paymentOrder, PaymentCallbackRecord latestRecord) {
        String channelTransactionNo = paymentOrder.getChannelTransactionNo() != null
                ? paymentOrder.getChannelTransactionNo()
                : latestRecord != null ? latestRecord.getChannelTransactionNo() : null;
        String channelStatus = paymentOrder.getChannelStatus() != null
                ? paymentOrder.getChannelStatus().value()
                : latestRecord != null ? latestRecord.getChannelStatus().value() : null;
        String callbackSummary = paymentOrder.getCallbackSummary() != null
                ? paymentOrder.getCallbackSummary()
                : latestRecord != null ? latestRecord.summarize() : null;
        return new PaymentDetailDTO(
                paymentOrder.getPaymentNo().value(),
                paymentOrder.getOrderNo().value(),
                paymentOrder.getUserId() == null
                        ? null
                        : paymentOrder.getUserId().value(),
                paymentOrder.getChannelCode().value(),
                paymentOrder.getPaymentStatus().value(),
                paymentOrder.getAmount().value(),
                paymentOrder.getPaidAmount().value(),
                paymentOrder.getCreatedAt(),
                paymentOrder.getExpiredAt(),
                paymentOrder.getPaidAt(),
                paymentOrder.getSubject(),
                paymentOrder.getClosedAt(),
                channelTransactionNo,
                channelStatus,
                callbackSummary);
    }
}
