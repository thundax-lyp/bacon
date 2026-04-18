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
    public PaymentCallbackRecord insert(PaymentCallbackRecord callbackRecord) {
        return support.insert(callbackRecord);
    }

    @Override
    public Optional<PaymentCallbackRecord> findLatestByPaymentNo(String paymentNo) {
        return support.findLatestByPaymentNo(paymentNo);
    }

    @Override
    public Optional<PaymentCallbackRecord> findByChannelTransactionNo(
            String channelCode, String channelTransactionNo) {
        return support.findByChannelTransactionNo(channelCode, channelTransactionNo);
    }

    @Override
    public List<PaymentCallbackRecord> listByPaymentNo(String paymentNo) {
        return support.listByPaymentNo(paymentNo);
    }
}
