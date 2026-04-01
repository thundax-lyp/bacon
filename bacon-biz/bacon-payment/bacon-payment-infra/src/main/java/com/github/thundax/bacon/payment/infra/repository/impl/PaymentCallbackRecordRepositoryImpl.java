package com.github.thundax.bacon.payment.infra.repository.impl;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.repository.PaymentCallbackRecordRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentCallbackRecordRepositoryImpl implements PaymentCallbackRecordRepository {

    private final PaymentRepositorySupport support;

    public PaymentCallbackRecordRepositoryImpl(PaymentRepositorySupport support) {
        this.support = support;
    }

    @Override
    public PaymentCallbackRecord save(PaymentCallbackRecord callbackRecord) {
        return support.saveCallbackRecord(callbackRecord);
    }

    @Override
    public Optional<PaymentCallbackRecord> findLatestCallbackByPaymentNo(Long tenantId, String paymentNo) {
        return support.findLatestCallbackByPaymentNo(tenantId, paymentNo);
    }

    @Override
    public Optional<PaymentCallbackRecord> findCallbackByChannelTransactionNo(Long tenantId, String channelCode,
                                                                              String channelTransactionNo) {
        return support.findCallbackByChannelTransactionNo(tenantId, channelCode, channelTransactionNo);
    }

    @Override
    public List<PaymentCallbackRecord> findCallbacksByPaymentNo(Long tenantId, String paymentNo) {
        return support.findCallbacksByPaymentNo(tenantId, paymentNo);
    }
}
