package com.github.thundax.bacon.payment.infra.repository.impl;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentCallbackRecord;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentOrder;
import com.github.thundax.bacon.payment.domain.repository.PaymentAuditLogRepository;
import com.github.thundax.bacon.payment.domain.repository.PaymentCallbackRecordRepository;
import com.github.thundax.bacon.payment.domain.repository.PaymentOrderRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPaymentRepository implements PaymentOrderRepository, PaymentCallbackRecordRepository, PaymentAuditLogRepository {

    private final Map<String, PaymentOrder> paymentsByPaymentNo = new ConcurrentHashMap<>();
    private final Map<String, PaymentOrder> paymentsByOrderNo = new ConcurrentHashMap<>();
    private final Map<String, List<PaymentCallbackRecord>> callbackRecordsByPaymentNo = new ConcurrentHashMap<>();
    private final Map<String, PaymentCallbackRecord> callbackRecordsByTxn = new ConcurrentHashMap<>();
    private final Map<String, List<PaymentAuditLog>> auditLogsByPaymentNo = new ConcurrentHashMap<>();

    @Override
    public PaymentOrder save(PaymentOrder paymentOrder) {
        paymentsByPaymentNo.put(paymentKey(paymentOrder.getTenantId(), paymentOrder.getPaymentNo()), paymentOrder);
        paymentsByOrderNo.put(orderKey(paymentOrder.getTenantId(), paymentOrder.getOrderNo()), paymentOrder);
        return paymentOrder;
    }

    @Override
    public Optional<PaymentOrder> findOrderByPaymentNo(Long tenantId, String paymentNo) {
        return Optional.ofNullable(paymentsByPaymentNo.get(paymentKey(tenantId, paymentNo)));
    }

    @Override
    public Optional<PaymentOrder> findOrderByOrderNo(Long tenantId, String orderNo) {
        return Optional.ofNullable(paymentsByOrderNo.get(orderKey(tenantId, orderNo)));
    }

    @Override
    public PaymentCallbackRecord save(PaymentCallbackRecord callbackRecord) {
        callbackRecordsByPaymentNo.computeIfAbsent(paymentKey(callbackRecord.getTenantId(), callbackRecord.getPaymentNo()),
                ignored -> new ArrayList<>()).add(callbackRecord);
        if (callbackRecord.getChannelTransactionNo() != null && !callbackRecord.getChannelTransactionNo().isBlank()) {
            callbackRecordsByTxn.put(txnKey(callbackRecord.getTenantId(), callbackRecord.getChannelCode(),
                    callbackRecord.getChannelTransactionNo()), callbackRecord);
        }
        return callbackRecord;
    }

    @Override
    public Optional<PaymentCallbackRecord> findLatestCallbackByPaymentNo(Long tenantId, String paymentNo) {
        return findCallbacksByPaymentNo(tenantId, paymentNo).stream()
                .max(Comparator.comparing(PaymentCallbackRecord::getReceivedAt).thenComparing(PaymentCallbackRecord::getId));
    }

    @Override
    public Optional<PaymentCallbackRecord> findCallbackByChannelTransactionNo(Long tenantId, String channelCode,
                                                                              String channelTransactionNo) {
        return Optional.ofNullable(callbackRecordsByTxn.get(txnKey(tenantId, channelCode, channelTransactionNo)));
    }

    @Override
    public List<PaymentCallbackRecord> findCallbacksByPaymentNo(Long tenantId, String paymentNo) {
        return List.copyOf(callbackRecordsByPaymentNo.getOrDefault(paymentKey(tenantId, paymentNo), List.of()));
    }

    @Override
    public void save(PaymentAuditLog auditLog) {
        auditLogsByPaymentNo.computeIfAbsent(paymentKey(auditLog.getTenantId(), auditLog.getPaymentNo()),
                ignored -> new ArrayList<>()).add(auditLog);
    }

    @Override
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
