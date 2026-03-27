package com.github.thundax.bacon.payment.application.service;

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

    public PaymentQueryApplicationService(PaymentOrderRepository paymentOrderRepository,
                                          PaymentCallbackRecordRepository paymentCallbackRecordRepository) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.paymentCallbackRecordRepository = paymentCallbackRecordRepository;
    }

    public PaymentDetailDTO getByPaymentNo(Long tenantId, String paymentNo) {
        return toDetail(paymentOrderRepository.findOrderByPaymentNo(tenantId, paymentNo)
                .orElseThrow(() -> new PaymentDomainException(PaymentErrorCode.PAYMENT_NOT_FOUND, paymentNo)));
    }

    public PaymentDetailDTO getByOrderNo(Long tenantId, String orderNo) {
        return toDetail(paymentOrderRepository.findOrderByOrderNo(tenantId, orderNo)
                .orElseThrow(() -> new PaymentDomainException(PaymentErrorCode.PAYMENT_NOT_FOUND, orderNo)));
    }

    private PaymentDetailDTO toDetail(PaymentOrder paymentOrder) {
        PaymentCallbackRecord latestRecord = paymentCallbackRecordRepository
                .findLatestCallbackByPaymentNo(paymentOrder.getTenantId(), paymentOrder.getPaymentNo())
                .orElse(null);
        return new PaymentDetailDTO(paymentOrder.getTenantId(), paymentOrder.getPaymentNo(), paymentOrder.getOrderNo(),
                paymentOrder.getUserId(), paymentOrder.getChannelCode(), paymentOrder.getPaymentStatus(),
                paymentOrder.getAmount(), paymentOrder.getPaidAmount(), paymentOrder.getCreatedAt(),
                paymentOrder.getExpiredAt(), paymentOrder.getPaidAt(), paymentOrder.getSubject(),
                paymentOrder.getClosedAt(),
                latestRecord != null ? latestRecord.getChannelTransactionNo() : paymentOrder.getChannelTransactionNo(),
                latestRecord != null ? latestRecord.getChannelStatus() : paymentOrder.getChannelStatus(),
                latestRecord != null ? latestRecord.summarize() : paymentOrder.getCallbackSummary());
    }
}
