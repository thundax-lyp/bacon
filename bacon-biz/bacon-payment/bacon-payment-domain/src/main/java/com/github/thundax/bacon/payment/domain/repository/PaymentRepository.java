package com.github.thundax.bacon.payment.domain.repository;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;

import java.util.Optional;

public interface PaymentRepository {

    PaymentOrder save(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByPaymentNo(Long tenantId, String paymentNo);

    Optional<PaymentOrder> findByOrderNo(Long tenantId, String orderNo);
}
