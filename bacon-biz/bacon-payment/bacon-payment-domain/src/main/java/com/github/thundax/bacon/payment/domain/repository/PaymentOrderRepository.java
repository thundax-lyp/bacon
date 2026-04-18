package com.github.thundax.bacon.payment.domain.repository;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import java.util.Optional;

public interface PaymentOrderRepository {

    PaymentOrder insert(PaymentOrder paymentOrder);

    PaymentOrder update(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findByPaymentNo(String paymentNo);

    Optional<PaymentOrder> findByOrderNo(String orderNo);
}
