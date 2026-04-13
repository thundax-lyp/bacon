package com.github.thundax.bacon.payment.infra.repository.impl;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.repository.PaymentCallbackRecordRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
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
    public Optional<PaymentCallbackRecord> findLatestCallbackByPaymentNo(String paymentNo) {
        return support.findLatestCallbackByPaymentNo(paymentNo);
    }

    @Override
    public Optional<PaymentCallbackRecord> findCallbackByChannelTransactionNo(String channelCode, String channelTransactionNo) {
        return support.findCallbackByChannelTransactionNo(channelCode, channelTransactionNo);
    }

    @Override
    public List<PaymentCallbackRecord> findCallbacksByPaymentNo(String paymentNo) {
        return support.findCallbacksByPaymentNo(paymentNo);
    }
}
