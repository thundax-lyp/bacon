package com.github.thundax.bacon.payment.infra.repository.impl;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("test")
public class InMemoryPaymentRepositorySupport {

    private final AtomicLong paymentOrderIdGenerator = new AtomicLong(1000L);
    private final AtomicLong callbackRecordIdGenerator = new AtomicLong(1000L);
    private final AtomicLong auditLogIdGenerator = new AtomicLong(1000L);
    private final Map<String, PaymentOrder> paymentsByPaymentNo = new ConcurrentHashMap<>();
    private final Map<String, PaymentOrder> paymentsByOrderNo = new ConcurrentHashMap<>();
    private final Map<String, List<PaymentCallbackRecord>> callbackRecordsByPaymentNo = new ConcurrentHashMap<>();
    private final Map<String, PaymentCallbackRecord> callbackRecordsByTxn = new ConcurrentHashMap<>();
    private final Map<String, List<PaymentAuditLog>> auditLogsByPaymentNo = new ConcurrentHashMap<>();

    public InMemoryPaymentRepositorySupport() {
        log.info("Using in-memory payment repository");
    }

    public PaymentOrder saveOrder(PaymentOrder paymentOrder) {
        PaymentOrder persisted = paymentOrder.getId() == null
                ? PaymentOrder.rehydrate(paymentOrderIdGenerator.getAndIncrement(), paymentOrder.getTenantId(),
                paymentOrder.getPaymentNo(), paymentOrder.getOrderNo(), paymentOrder.getUserId(),
                paymentOrder.getChannelCode(), paymentOrder.getAmount(), paymentOrder.getPaidAmount(),
                paymentOrder.getSubject(), paymentOrder.getCreatedAt(), paymentOrder.getExpiredAt(),
                paymentOrder.getPaidAt(), paymentOrder.getClosedAt(), paymentOrder.getPaymentStatus(),
                paymentOrder.getChannelTransactionNo(), paymentOrder.getChannelStatus(), paymentOrder.getCallbackSummary())
                : paymentOrder;
        paymentsByPaymentNo.put(paymentKey(persisted.getTenantId(), persisted.getPaymentNo()), persisted);
        paymentsByOrderNo.put(orderKey(persisted.getTenantId(), persisted.getOrderNo()), persisted);
        return persisted;
    }

    public Optional<PaymentOrder> findOrderByPaymentNo(Long tenantId, String paymentNo) {
        return Optional.ofNullable(paymentsByPaymentNo.get(paymentKey(tenantId, paymentNo)));
    }

    public Optional<PaymentOrder> findOrderByOrderNo(Long tenantId, String orderNo) {
        return Optional.ofNullable(paymentsByOrderNo.get(orderKey(tenantId, orderNo)));
    }

    public PaymentCallbackRecord saveCallbackRecord(PaymentCallbackRecord callbackRecord) {
        PaymentCallbackRecord persisted = callbackRecord.getId() == null
                ? new PaymentCallbackRecord(callbackRecordIdGenerator.getAndIncrement(), callbackRecord.getTenantId(),
                callbackRecord.getPaymentNo(), callbackRecord.getOrderNo(), callbackRecord.getChannelCode(),
                callbackRecord.getChannelTransactionNo(), callbackRecord.getChannelStatus(),
                callbackRecord.getRawPayload(), callbackRecord.getReceivedAt())
                : callbackRecord;
        callbackRecordsByPaymentNo.computeIfAbsent(paymentKey(persisted.getTenantId(), persisted.getPaymentNo()),
                ignored -> new ArrayList<>()).add(persisted);
        if (persisted.getChannelTransactionNo() != null && !persisted.getChannelTransactionNo().isBlank()) {
            callbackRecordsByTxn.put(txnKey(persisted.getTenantId(), persisted.getChannelCode(),
                    persisted.getChannelTransactionNo()), persisted);
        }
        return persisted;
    }

    public Optional<PaymentCallbackRecord> findLatestCallbackByPaymentNo(Long tenantId, String paymentNo) {
        return findCallbacksByPaymentNo(tenantId, paymentNo).stream()
                .max(Comparator.comparing(PaymentCallbackRecord::getReceivedAt).thenComparing(PaymentCallbackRecord::getId));
    }

    public Optional<PaymentCallbackRecord> findCallbackByChannelTransactionNo(Long tenantId, String channelCode,
                                                                              String channelTransactionNo) {
        return Optional.ofNullable(callbackRecordsByTxn.get(txnKey(tenantId, channelCode, channelTransactionNo)));
    }

    public List<PaymentCallbackRecord> findCallbacksByPaymentNo(Long tenantId, String paymentNo) {
        return List.copyOf(callbackRecordsByPaymentNo.getOrDefault(paymentKey(tenantId, paymentNo), List.of()));
    }

    public void saveAuditLog(PaymentAuditLog auditLog) {
        PaymentAuditLog persisted = auditLog.getId() == null
                ? new PaymentAuditLog(auditLogIdGenerator.getAndIncrement(), auditLog.getTenantId(),
                auditLog.getPaymentNo(), auditLog.getActionType(), auditLog.getBeforeStatus(),
                auditLog.getAfterStatus(), auditLog.getOperatorType(), auditLog.getOperatorId(),
                auditLog.getOccurredAt())
                : auditLog;
        auditLogsByPaymentNo.computeIfAbsent(paymentKey(persisted.getTenantId(), persisted.getPaymentNo()),
                ignored -> new ArrayList<>()).add(persisted);
    }

    public List<PaymentAuditLog> findAuditLogsByPaymentNo(Long tenantId, String paymentNo) {
        return List.copyOf(auditLogsByPaymentNo.getOrDefault(paymentKey(tenantId, paymentNo), List.of()));
    }

    private static String paymentKey(Long tenantId, String paymentNo) {
        return tenantId + ":" + paymentNo;
    }

    private static String orderKey(Long tenantId, String orderNo) {
        return tenantId + ":" + orderNo;
    }

    private static String txnKey(Long tenantId, String channelCode, String channelTransactionNo) {
        return tenantId + ":" + channelCode + ":" + channelTransactionNo;
    }
}
