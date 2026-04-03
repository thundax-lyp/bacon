package com.github.thundax.bacon.payment.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.PaymentOrderId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryPaymentRepositorySupportTest {

    @Test
    void shouldPersistPaymentOrderAndSupportOrderScopedIdempotentLookup() {
        InMemoryPaymentRepositorySupport repository = new InMemoryPaymentRepositorySupport();
        PaymentOrder paymentOrder = new PaymentOrder(null, TenantId.of("1001"), "PAY-10001", "ORD-10001",
                UserId.of("2001"),
                "MOCK", new BigDecimal("88.80"), "test-payment",
                Instant.parse("2026-03-27T10:30:00Z"), Instant.parse("2026-03-27T10:00:00Z"));
        paymentOrder.markPaying();

        PaymentOrder persisted = repository.saveOrder(paymentOrder);

        assertEquals(PaymentOrderId.of("1000"), persisted.getId());
        assertEquals("PAY-10001", repository.findOrderByPaymentNo(1001L, "PAY-10001").orElseThrow().getPaymentNo());
        assertEquals("ORD-10001", repository.findOrderByOrderNo(1001L, "ORD-10001").orElseThrow().getOrderNo());
    }

    @Test
    void shouldKeepLatestCallbackAndAuditLogOrdering() {
        InMemoryPaymentRepositorySupport repository = new InMemoryPaymentRepositorySupport();
        Instant first = Instant.parse("2026-03-27T10:01:00Z");
        Instant second = Instant.parse("2026-03-27T10:02:00Z");

        repository.saveCallbackRecord(new PaymentCallbackRecord(null, 1001L, "PAY-10002", "ORD-10002", "MOCK",
                "TXN-1", "PROCESSING", "{\"tradeStatus\":\"PROCESSING\"}", first));
        repository.saveCallbackRecord(new PaymentCallbackRecord(null, 1001L, "PAY-10002", "ORD-10002", "MOCK",
                "TXN-2", "SUCCESS", "{\"tradeStatus\":\"SUCCESS\"}", second));

        repository.saveAuditLog(new PaymentAuditLog(null, 1001L, "PAY-10002",
                PaymentAuditLog.ACTION_CREATE, null, "PAYING", PaymentAuditLog.OPERATOR_SYSTEM, 0L, first));
        repository.saveAuditLog(new PaymentAuditLog(null, 1001L, "PAY-10002",
                PaymentAuditLog.ACTION_CALLBACK_PAID, "PAYING", "PAID", PaymentAuditLog.OPERATOR_CHANNEL, 0L, second));

        PaymentCallbackRecord latest = repository.findLatestCallbackByPaymentNo(1001L, "PAY-10002").orElseThrow();
        List<PaymentCallbackRecord> callbacks = repository.findCallbacksByPaymentNo(1001L, "PAY-10002");
        List<PaymentAuditLog> auditLogs = repository.findAuditLogsByPaymentNo(1001L, "PAY-10002");

        assertEquals("TXN-2", latest.getChannelTransactionNo());
        assertEquals(2, callbacks.size());
        assertEquals(2, auditLogs.size());
        assertEquals(PaymentAuditLog.ACTION_CREATE, auditLogs.get(0).getActionType());
        assertEquals(PaymentAuditLog.ACTION_CALLBACK_PAID, auditLogs.get(1).getActionType());
    }

    @Test
    void shouldFindCallbackByChannelTransactionNo() {
        InMemoryPaymentRepositorySupport repository = new InMemoryPaymentRepositorySupport();
        repository.saveCallbackRecord(new PaymentCallbackRecord(null, 1001L, "PAY-10003", "ORD-10003", "MOCK",
                "TXN-30001", "SUCCESS", "{\"tradeStatus\":\"SUCCESS\"}",
                Instant.parse("2026-03-27T10:03:00Z")));

        PaymentCallbackRecord callbackRecord = repository.findCallbackByChannelTransactionNo(1001L, "MOCK", "TXN-30001")
                .orElseThrow();

        assertEquals("PAY-10003", callbackRecord.getPaymentNo());
        assertEquals("SUCCESS", callbackRecord.getChannelStatus());
    }
}
