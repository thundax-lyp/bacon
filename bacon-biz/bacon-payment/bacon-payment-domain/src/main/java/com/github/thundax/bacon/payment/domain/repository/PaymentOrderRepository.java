package com.github.thundax.bacon.payment.domain.repository;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import java.util.Optional;

public interface PaymentOrderRepository {

    PaymentOrder save(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findOrderByPaymentNo(String paymentNo);

    Optional<PaymentOrder> findOrderByOrderNo(String orderNo);
}
