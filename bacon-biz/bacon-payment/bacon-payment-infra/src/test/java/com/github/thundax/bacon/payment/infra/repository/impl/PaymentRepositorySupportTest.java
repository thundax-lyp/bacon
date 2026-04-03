package com.github.thundax.bacon.payment.infra.repository.impl;

import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.PaymentOrderId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentChannelCode;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.payment.domain.model.valueobject.PaymentNo;
import com.github.thundax.bacon.payment.domain.exception.PaymentDomainException;
import com.github.thundax.bacon.payment.domain.exception.PaymentErrorCode;
import com.github.thundax.bacon.payment.infra.persistence.dataobject.PaymentAuditLogDO;
import com.github.thundax.bacon.payment.infra.persistence.dataobject.PaymentCallbackRecordDO;
import com.github.thundax.bacon.payment.infra.persistence.dataobject.PaymentOrderDO;
import com.github.thundax.bacon.payment.infra.persistence.mapper.PaymentAuditLogMapper;
import com.github.thundax.bacon.payment.infra.persistence.mapper.PaymentCallbackRecordMapper;
import com.github.thundax.bacon.payment.infra.persistence.mapper.PaymentOrderMapper;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentRepositorySupportTest {

    @Test
    void shouldInsertPaymentOrderAndMapGeneratedIdBackToDomain() {
        AtomicReference<PaymentOrderDO> insertedRef = new AtomicReference<>();
        PaymentRepositorySupport support = new PaymentRepositorySupport(
                createOrderMapper(insertedRef, null, null),
                createCallbackMapper(null, null, null),
                createAuditLogMapper(null, null),
                createIdGenerator()
        );

        PaymentOrder persisted = support.saveOrder(new PaymentOrder(null, TenantId.of("1001"), PaymentNo.of("PAY-10001"),
                OrderNo.of("ORD-10001"), UserId.of("2001"),
                PaymentChannelCode.MOCK, Money.of(new BigDecimal("88.80")), "strict-insert",
                Instant.parse("2026-03-27T10:30:00Z"), Instant.parse("2026-03-27T10:00:00Z")));

        assertNotNull(insertedRef.get());
        assertEquals("PAY-10001", insertedRef.get().getPaymentNo());
        assertEquals(PaymentOrderId.of("8001"), persisted.getId());
        assertEquals("ORD-10001", persisted.getOrderNo().value());
    }

    @Test
    void shouldUpdateExistingPaymentOrder() {
        AtomicReference<PaymentOrderDO> updatedRef = new AtomicReference<>();
        PaymentRepositorySupport support = new PaymentRepositorySupport(
                createOrderMapper(null, updatedRef, null),
                createCallbackMapper(null, null, null),
                createAuditLogMapper(null, null),
                createIdGenerator()
        );
        PaymentOrder paymentOrder = PaymentOrder.rehydrate(PaymentOrderId.of("9001"), TenantId.of("1001"),
                PaymentNo.of("PAY-10002"), OrderNo.of("ORD-10002"),
                UserId.of("2002"),
                PaymentChannelCode.MOCK, Money.of(new BigDecimal("99.90")), Money.zero(), "strict-update",
                Instant.parse("2026-03-27T10:05:00Z"), Instant.parse("2026-03-27T10:35:00Z"),
                null, null, PaymentStatus.PAYING, null, null, null);
        paymentOrder.close(Instant.parse("2026-03-27T10:15:00Z"));

        PaymentOrder persisted = support.saveOrder(paymentOrder);

        assertNotNull(updatedRef.get());
        assertEquals(9001L, updatedRef.get().getId());
        assertEquals(PaymentStatus.CLOSED.value(), updatedRef.get().getPaymentStatus());
        assertEquals(PaymentStatus.CLOSED.value(), persisted.getPaymentStatus().value());
    }

    @Test
    void shouldThrowConflictWhenStrictUpdateAffectsNoRows() {
        PaymentRepositorySupport support = new PaymentRepositorySupport(
                createOrderMapper(null, null, null, 0),
                createCallbackMapper(null, null, null),
                createAuditLogMapper(null, null),
                createIdGenerator()
        );
        PaymentOrder paymentOrder = PaymentOrder.rehydrate(PaymentOrderId.of("9002"), TenantId.of("1001"),
                PaymentNo.of("PAY-10009"), OrderNo.of("ORD-10009"),
                UserId.of("2009"),
                PaymentChannelCode.MOCK, Money.of(new BigDecimal("66.00")), Money.zero(), "strict-conflict",
                Instant.parse("2026-03-27T10:05:00Z"), Instant.parse("2026-03-27T10:35:00Z"),
                null, null, PaymentStatus.PAYING, null, null, null);
        paymentOrder.close(Instant.parse("2026-03-27T10:15:00Z"));

        PaymentDomainException ex = assertThrows(PaymentDomainException.class, () -> support.saveOrder(paymentOrder));

        assertEquals(PaymentErrorCode.PAYMENT_PERSISTENCE_CONFLICT.code(), ex.getCode());
    }

    @Test
    void shouldMapStrictReadModelsFromMappers() {
        PaymentOrderDO orderDataObject = new PaymentOrderDO(9101L, "1001", "PAY-10003", "ORD-10003", "2003",
                "MOCK", PaymentStatus.PAID.value(), new BigDecimal("128.00"), new BigDecimal("128.00"),
                "strict-read", Instant.parse("2026-03-27T10:10:00Z"), Instant.parse("2026-03-27T10:11:00Z"),
                Instant.parse("2026-03-27T10:40:00Z"), Instant.parse("2026-03-27T10:11:30Z"), null);
        PaymentCallbackRecordDO callbackDataObject = new PaymentCallbackRecordDO(9201L, "1001", "PAY-10003", "ORD-10003",
                "MOCK", "TXN-10003", "SUCCESS", "{\"tradeStatus\":\"SUCCESS\"}",
                Instant.parse("2026-03-27T10:11:20Z"));
        PaymentAuditLogDO createLog = new PaymentAuditLogDO(9301L, "1001", "PAY-10003", PaymentAuditLog.ACTION_CREATE,
                null, PaymentStatus.PAYING.value(), PaymentAuditLog.OPERATOR_SYSTEM, "0",
                Instant.parse("2026-03-27T10:10:00Z"));
        PaymentAuditLogDO paidLog = new PaymentAuditLogDO(9302L, "1001", "PAY-10003", PaymentAuditLog.ACTION_CALLBACK_PAID,
                PaymentStatus.PAYING.value(), PaymentStatus.PAID.value(), PaymentAuditLog.OPERATOR_CHANNEL, "0",
                Instant.parse("2026-03-27T10:11:30Z"));

        PaymentRepositorySupport support = new PaymentRepositorySupport(
                createOrderMapper(null, null, orderDataObject),
                createCallbackMapper(callbackDataObject, callbackDataObject, List.of(callbackDataObject)),
                createAuditLogMapper(null, List.of(createLog, paidLog)),
                createIdGenerator()
        );

        PaymentOrder paymentOrder = support.findOrderByPaymentNo(1001L, "PAY-10003").orElseThrow();
        PaymentCallbackRecord latestCallback = support.findLatestCallbackByPaymentNo(1001L, "PAY-10003").orElseThrow();
        List<PaymentAuditLog> auditLogs = support.findAuditLogsByPaymentNo(1001L, "PAY-10003");

        assertEquals(PaymentStatus.PAID.value(), paymentOrder.getPaymentStatus().value());
        assertEquals("TXN-10003", latestCallback.getChannelTransactionNo());
        assertEquals(2, auditLogs.size());
        assertEquals(PaymentAuditLog.ACTION_CREATE, auditLogs.get(0).getActionType());
        assertEquals(PaymentAuditLog.ACTION_CALLBACK_PAID, auditLogs.get(1).getActionType());
    }

    @SuppressWarnings("unchecked")
    private PaymentOrderMapper createOrderMapper(AtomicReference<PaymentOrderDO> insertedRef,
                                                 AtomicReference<PaymentOrderDO> updatedRef,
                                                 PaymentOrderDO selectedOrder) {
        return createOrderMapper(insertedRef, updatedRef, selectedOrder, 1);
    }

    @SuppressWarnings("unchecked")
    private PaymentOrderMapper createOrderMapper(AtomicReference<PaymentOrderDO> insertedRef,
                                                 AtomicReference<PaymentOrderDO> updatedRef,
                                                 PaymentOrderDO selectedOrder,
                                                 int updateRows) {
        return (PaymentOrderMapper) Proxy.newProxyInstance(PaymentOrderMapper.class.getClassLoader(),
                new Class[]{PaymentOrderMapper.class}, (proxy, method, args) -> {
                    if ("insert".equals(method.getName())) {
                        PaymentOrderDO dataObject = (PaymentOrderDO) args[0];
                        dataObject.setId(8001L);
                        insertedRef.set(dataObject);
                        return 1;
                    }
                    if ("updateById".equals(method.getName())) {
                        if (updatedRef != null) {
                            updatedRef.set((PaymentOrderDO) args[0]);
                        }
                        return updateRows;
                    }
                    if ("selectOne".equals(method.getName())) {
                        return selectedOrder;
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private PaymentCallbackRecordMapper createCallbackMapper(PaymentCallbackRecordDO singleSelected,
                                                             PaymentCallbackRecordDO transactionSelected,
                                                             List<PaymentCallbackRecordDO> listSelected) {
        AtomicInteger selectListCalls = new AtomicInteger(0);
        return (PaymentCallbackRecordMapper) Proxy.newProxyInstance(PaymentCallbackRecordMapper.class.getClassLoader(),
                new Class[]{PaymentCallbackRecordMapper.class}, (proxy, method, args) -> {
                    if ("insert".equals(method.getName())) {
                        PaymentCallbackRecordDO dataObject = (PaymentCallbackRecordDO) args[0];
                        return 1;
                    }
                    if ("updateById".equals(method.getName())) {
                        return 1;
                    }
                    if ("selectOne".equals(method.getName())) {
                        return transactionSelected;
                    }
                    if ("selectList".equals(method.getName())) {
                        return selectListCalls.getAndIncrement() == 0
                                ? singleSelected == null ? List.of() : List.of(singleSelected)
                                : listSelected == null ? List.of() : listSelected;
                    }
                    return null;
                });
    }

    private IdGenerator createIdGenerator() {
        return bizTag -> 8201L;
    }

    @SuppressWarnings("unchecked")
    private PaymentAuditLogMapper createAuditLogMapper(AtomicReference<PaymentAuditLogDO> insertedRef,
                                                       List<PaymentAuditLogDO> selectedLogs) {
        return (PaymentAuditLogMapper) Proxy.newProxyInstance(PaymentAuditLogMapper.class.getClassLoader(),
                new Class[]{PaymentAuditLogMapper.class}, (proxy, method, args) -> {
                    if ("insert".equals(method.getName())) {
                        PaymentAuditLogDO dataObject = (PaymentAuditLogDO) args[0];
                        if (insertedRef != null) {
                            insertedRef.set(dataObject);
                        }
                        return 1;
                    }
                    if ("updateById".equals(method.getName())) {
                        return 1;
                    }
                    if ("selectList".equals(method.getName())) {
                        return selectedLogs;
                    }
                    return null;
                });
    }
}
