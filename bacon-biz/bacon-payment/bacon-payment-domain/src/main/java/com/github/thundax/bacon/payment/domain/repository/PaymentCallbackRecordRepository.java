package com.github.thundax.bacon.payment.domain.repository;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import java.util.List;
import java.util.Optional;

public interface PaymentCallbackRecordRepository {

    PaymentCallbackRecord insert(PaymentCallbackRecord callbackRecord);

    Optional<PaymentCallbackRecord> findLatestByPaymentNo(String paymentNo);

    Optional<PaymentCallbackRecord> findByChannelTransactionNo(String channelCode, String channelTransactionNo);

    List<PaymentCallbackRecord> listByPaymentNo(String paymentNo);
}
