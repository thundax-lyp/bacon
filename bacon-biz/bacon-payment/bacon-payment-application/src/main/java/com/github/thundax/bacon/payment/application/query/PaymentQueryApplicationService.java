package com.github.thundax.bacon.payment.application.query;

import com.github.thundax.bacon.payment.application.assembler.PaymentOrderAssembler;
import com.github.thundax.bacon.payment.application.dto.PaymentDetailDTO;
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

    public PaymentDetailDTO getByPaymentNo(PaymentGetByPaymentNoQuery query) {
        return toDetail(paymentOrderRepository
                .findByPaymentNo(query.paymentNo())
                .orElseThrow(() -> new PaymentDomainException(PaymentErrorCode.PAYMENT_NOT_FOUND, query.paymentNo())));
    }

    public PaymentDetailDTO getByOrderNo(PaymentGetByOrderNoQuery query) {
        return toDetail(paymentOrderRepository
                .findByOrderNo(query.orderNo())
                .orElseThrow(() -> new PaymentDomainException(PaymentErrorCode.PAYMENT_NOT_FOUND, query.orderNo())));
    }

    private PaymentDetailDTO toDetail(PaymentOrder paymentOrder) {
        PaymentCallbackRecord latestRecord = paymentCallbackRecordRepository
                .findLatestByPaymentNo(paymentOrder.getPaymentNo().value())
                .orElse(null);
        return PaymentOrderAssembler.toDetail(paymentOrder, latestRecord);
    }
}
