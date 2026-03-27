package com.github.thundax.bacon.payment.domain.repository;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;

import java.util.List;
import java.util.Optional;

public interface PaymentCallbackRecordRepository {

    PaymentCallbackRecord save(PaymentCallbackRecord callbackRecord);

    Optional<PaymentCallbackRecord> findLatestCallbackByPaymentNo(Long tenantId, String paymentNo);

    Optional<PaymentCallbackRecord> findCallbackByChannelTransactionNo(Long tenantId, String channelCode,
                                                                       String channelTransactionNo);

    List<PaymentCallbackRecord> findCallbacksByPaymentNo(Long tenantId, String paymentNo);
}
