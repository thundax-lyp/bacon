package com.github.thundax.bacon.payment.infra.repository.impl;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.repository.PaymentAuditLogRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentAuditLogRepositoryImpl implements PaymentAuditLogRepository {

    private final PaymentRepositorySupport support;

    public PaymentAuditLogRepositoryImpl(PaymentRepositorySupport support) {
        this.support = support;
    }

    @Override
    public void save(PaymentAuditLog auditLog) {
        support.saveAuditLog(auditLog);
    }

    @Override
    public List<PaymentAuditLog> findAuditLogsByPaymentNo(Long tenantId, String paymentNo) {
        return support.findAuditLogsByPaymentNo(tenantId, paymentNo);
    }
}
