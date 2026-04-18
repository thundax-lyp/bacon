package com.github.thundax.bacon.payment.infra.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditActionType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditOperatorType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelCode;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.domain.model.valueobject.PaymentOrderId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryPaymentRepositorySupportTest {

    @BeforeEach
    void setUp() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
    }

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldPersistPaymentOrderAndSupportOrderScopedIdempotentLookup() {
        InMemoryPaymentRepositorySupport repository = new InMemoryPaymentRepositorySupport();
        PaymentOrder paymentOrder = PaymentOrder.create(
                PaymentOrderId.of(1000L),
                PaymentNo.of("PAY-10001"),
                OrderNo.of("ORD-10001"),
                UserId.of(2001L),
                PaymentChannelCode.MOCK,
                Money.of(new BigDecimal("88.80")),
                "test-payment",
                Instant.parse("2026-03-27T10:30:00Z"),
                Instant.parse("2026-03-27T10:00:00Z"));
        paymentOrder.markPaying();

        PaymentOrder persisted = repository.insert(paymentOrder);

        assertEquals(PaymentOrderId.of(1000L), persisted.getId());
        assertEquals(
                "PAY-10001",
                repository
                        .findByPaymentNo("PAY-10001")
                        .orElseThrow()
                        .getPaymentNo()
                        .value());
        assertEquals(
                "ORD-10001",
                repository
                        .findByOrderNo("ORD-10001")
                        .orElseThrow()
                        .getOrderNo()
                        .value());
    }

    @Test
    void shouldKeepLatestCallbackAndAuditLogOrdering() {
        InMemoryPaymentRepositorySupport repository = new InMemoryPaymentRepositorySupport();
        Instant first = Instant.parse("2026-03-27T10:01:00Z");
        Instant second = Instant.parse("2026-03-27T10:02:00Z");

        repository.insert(PaymentCallbackRecord.create(
                1000L,
                PaymentNo.of("PAY-10002"),
                OrderNo.of("ORD-10002"),
                PaymentChannelCode.MOCK,
                "TXN-1",
                PaymentChannelStatus.PAYING,
                "{\"tradeStatus\":\"PROCESSING\"}",
                first));
        repository.insert(PaymentCallbackRecord.create(
                1001L,
                PaymentNo.of("PAY-10002"),
                OrderNo.of("ORD-10002"),
                PaymentChannelCode.MOCK,
                "TXN-2",
                PaymentChannelStatus.SUCCESS,
                "{\"tradeStatus\":\"SUCCESS\"}",
                second));

        repository.insert(PaymentAuditLog.create(
                1000L,
                PaymentNo.of("PAY-10002"),
                PaymentAuditActionType.CREATE,
                null,
                PaymentStatus.PAYING,
                PaymentAuditOperatorType.SYSTEM,
                "0",
                first));
        repository.insert(PaymentAuditLog.create(
                1001L,
                PaymentNo.of("PAY-10002"),
                PaymentAuditActionType.CALLBACK_PAID,
                PaymentStatus.PAYING,
                PaymentStatus.PAID,
                PaymentAuditOperatorType.CHANNEL,
                "0",
                second));

        PaymentCallbackRecord latest =
                repository.findLatestByPaymentNo("PAY-10002").orElseThrow();
        List<PaymentCallbackRecord> callbacks = repository.listByPaymentNo("PAY-10002");
        List<PaymentAuditLog> auditLogs = repository.listLogsByPaymentNo("PAY-10002");

        assertEquals("TXN-2", latest.getChannelTransactionNo());
        assertEquals(2, callbacks.size());
        assertEquals(2, auditLogs.size());
        assertEquals(PaymentAuditActionType.CREATE, auditLogs.get(0).getActionType());
        assertEquals(PaymentAuditActionType.CALLBACK_PAID, auditLogs.get(1).getActionType());
    }

    @Test
    void shouldFindCallbackByChannelTransactionNo() {
        InMemoryPaymentRepositorySupport repository = new InMemoryPaymentRepositorySupport();
        repository.insert(PaymentCallbackRecord.create(
                1002L,
                PaymentNo.of("PAY-10003"),
                OrderNo.of("ORD-10003"),
                PaymentChannelCode.MOCK,
                "TXN-30001",
                PaymentChannelStatus.SUCCESS,
                "{\"tradeStatus\":\"SUCCESS\"}",
                Instant.parse("2026-03-27T10:03:00Z")));

        PaymentCallbackRecord callbackRecord = repository
                .findByChannelTransactionNo("MOCK", "TXN-30001")
                .orElseThrow();

        assertEquals("PAY-10003", callbackRecord.getPaymentNo().value());
        assertEquals(PaymentChannelStatus.SUCCESS, callbackRecord.getChannelStatus());
    }
}
