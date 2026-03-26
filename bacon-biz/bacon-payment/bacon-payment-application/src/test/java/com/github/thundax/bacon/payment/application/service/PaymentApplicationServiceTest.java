package com.github.thundax.bacon.payment.application.service;

import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentApplicationServiceTest {

    @Test
    void createPaymentShouldGeneratePaymentNoInsideModule() {
        PaymentApplicationService service = new PaymentApplicationService(new TestPaymentRepository(),
                () -> "PAY-20001");

        PaymentCreateResultDTO result = service.createPayment(1001L, "ORD-10001", 2001L, BigDecimal.TEN,
                "MOCK", "test", Instant.now().plusSeconds(1800));

        assertEquals("PAY-20001", result.getPaymentNo());
        assertEquals("ORD-10001", result.getOrderNo());
    }

    private static final class TestPaymentRepository implements PaymentRepository {

        private final ConcurrentMap<String, PaymentOrder> paymentsByPaymentNo = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, PaymentOrder> paymentsByOrderNo = new ConcurrentHashMap<>();

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
}
