package com.github.thundax.bacon.payment.domain.repository;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import java.util.Optional;

public interface PaymentOrderRepository {

    PaymentOrder save(PaymentOrder paymentOrder);

    Optional<PaymentOrder> findOrderByPaymentNo(Long tenantId, String paymentNo);

    Optional<PaymentOrder> findOrderByOrderNo(Long tenantId, String orderNo);
}
