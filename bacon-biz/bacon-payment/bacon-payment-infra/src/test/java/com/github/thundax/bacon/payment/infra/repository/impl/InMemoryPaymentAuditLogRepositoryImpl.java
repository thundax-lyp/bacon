package com.github.thundax.bacon.payment.infra.repository.impl;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.repository.PaymentAuditLogRepository;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryPaymentAuditLogRepositoryImpl implements PaymentAuditLogRepository {

    private final InMemoryPaymentRepositorySupport support;

    public InMemoryPaymentAuditLogRepositoryImpl(InMemoryPaymentRepositorySupport support) {
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
