package com.github.thundax.bacon.payment.application.query;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.payment.api.dto.PaymentDetailDTO;
import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentCallbackRecordRepository;
import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentQueryApplicationService {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentCallbackRecordRepository paymentCallbackRecordRepository;

    public PaymentQueryApplicationService(
            PaymentOrderRepository paymentOrderRepository,
            PaymentCallbackRecordRepository paymentCallbackRecordRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentCallbackRecordRepository = paymentCallbackRecordRepository;
    }

    public PaymentDetailDTO getByPaymentNo(String paymentNo) {
        return toDetail(paymentOrderRepository
                .findOrderByPaymentNo(paymentNo)
                .orElseThrow(() -> new PaymentDomainException(PaymentErrorCode.PAYMENT_NOT_FOUND, paymentNo)));
    }

    public PaymentDetailDTO getByOrderNo(String orderNo) {
        return toDetail(paymentOrderRepository
                .findOrderByOrderNo(orderNo)
                .orElseThrow(() -> new PaymentDomainException(PaymentErrorCode.PAYMENT_NOT_FOUND, orderNo)));
    }

    private PaymentDetailDTO toDetail(PaymentOrder paymentOrder) {
        PaymentCallbackRecord latestRecord = paymentCallbackRecordRepository
                .findLatestCallbackByPaymentNo(paymentOrder.getPaymentNo().value())
                .orElse(null);
        // 详情查询优先使用主单快照字段；只有主单还没固化对应信息时，才退回最近回调记录补齐展示字段。
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
                toLongUserValue(paymentOrder.getUserId()),
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

    private Long toLongUserValue(UserId userId) {
        return userId == null ? null : Long.valueOf(userId.value());
    }
}
