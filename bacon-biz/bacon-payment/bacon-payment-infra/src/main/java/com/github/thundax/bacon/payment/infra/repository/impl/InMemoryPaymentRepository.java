package com.github.thundax.bacon.payment.infra.repository.impl;

import com.github.thundax.bacon.payment.domain.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<String, PaymentOrder> paymentsByPaymentNo = new ConcurrentHashMap<>();
    private final Map<String, PaymentOrder> paymentsByOrderNo = new ConcurrentHashMap<>();

    @Override
    public PaymentOrder save(PaymentOrder paymentOrder) {
        paymentsByPaymentNo.put(paymentKey(paymentOrder.getTenantId(), paymentOrder.getPaymentNo()), paymentOrder);
        paymentsByOrderNo.put(orderKey(paymentOrder.getTenantId(), paymentOrder.getOrderNo()), paymentOrder);
        return paymentOrder;
    }

    @Override
    public Optional<PaymentOrder> findByPaymentNo(Long tenantId, String paymentNo) {
        return Optional.ofNullable(paymentsByPaymentNo.get(paymentKey(tenantId, paymentNo)));
    }

    @Override
    public Optional<PaymentOrder> findByOrderNo(Long tenantId, String orderNo) {
        return Optional.ofNullable(paymentsByOrderNo.get(orderKey(tenantId, orderNo)));
    }

    private static String paymentKey(Long tenantId, String paymentNo) {
        return tenantId + ":" + paymentNo;
    }

    private static String orderKey(Long tenantId, String orderNo) {
        return tenantId + ":" + orderNo;
    }
}
