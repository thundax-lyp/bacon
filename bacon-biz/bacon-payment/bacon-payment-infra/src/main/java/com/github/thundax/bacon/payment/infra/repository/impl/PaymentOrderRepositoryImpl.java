package com.github.thundax.bacon.payment.infra.repository.impl;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(PaymentRepositorySupport.class)
public class PaymentOrderRepositoryImpl implements PaymentOrderRepository {

    private final PaymentRepositorySupport support;

    public PaymentOrderRepositoryImpl(PaymentRepositorySupport support) {
        this.support = support;
    }

    @Override
    public PaymentOrder save(PaymentOrder paymentOrder) {
        return support.saveOrder(paymentOrder);
    }

    @Override
    public Optional<PaymentOrder> findOrderByPaymentNo(Long tenantId, String paymentNo) {
        return support.findOrderByPaymentNo(tenantId, paymentNo);
    }

    @Override
    public Optional<PaymentOrder> findOrderByOrderNo(Long tenantId, String orderNo) {
        return support.findOrderByOrderNo(tenantId, orderNo);
    }
}
