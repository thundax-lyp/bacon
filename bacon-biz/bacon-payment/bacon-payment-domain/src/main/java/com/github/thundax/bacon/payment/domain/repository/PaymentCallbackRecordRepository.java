package com.github.thundax.bacon.payment.domain.repository;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import java.util.List;
import java.util.Optional;

public interface PaymentCallbackRecordRepository {

    PaymentCallbackRecord save(PaymentCallbackRecord callbackRecord);

    Optional<PaymentCallbackRecord> findLatestCallbackByPaymentNo(String paymentNo);

    Optional<PaymentCallbackRecord> findCallbackByChannelTransactionNo(String channelCode, String channelTransactionNo);

    List<PaymentCallbackRecord> findCallbacksByPaymentNo(String paymentNo);
}
