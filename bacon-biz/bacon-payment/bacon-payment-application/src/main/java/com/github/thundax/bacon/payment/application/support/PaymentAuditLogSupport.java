package com.github.thundax.bacon.payment.application.support;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.repository.PaymentAuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentAuditLogSupport {

    private final PaymentAuditLogRepository paymentAuditLogRepository;

    public PaymentAuditLogSupport(PaymentAuditLogRepository paymentAuditLogRepository) {
        this.paymentAuditLogRepository = paymentAuditLogRepository;
    }

    public void saveSafely(PaymentAuditLog auditLog) {
        try {
            paymentAuditLogRepository.save(auditLog);
        } catch (RuntimeException ex) {
            log.error("ALERT payment audit write failed, tenantId={}, paymentNo={}, actionType={}",
                    auditLog.getTenantId(), auditLog.getPaymentNo(), auditLog.getActionType(), ex);
        }
    }

    public void recordCreate(Long tenantId, String paymentNo, String afterStatus, java.time.Instant occurredAt) {
        saveSafely(new PaymentAuditLog(null, tenantId, paymentNo, PaymentAuditLog.ACTION_CREATE, null,
                afterStatus, PaymentAuditLog.OPERATOR_SYSTEM, 0L, occurredAt));
    }

    public void recordCallback(String actionType, Long tenantId, String paymentNo, String beforeStatus,
                               String afterStatus, java.time.Instant occurredAt) {
        saveSafely(new PaymentAuditLog(null, tenantId, paymentNo, actionType, beforeStatus, afterStatus,
                PaymentAuditLog.OPERATOR_CHANNEL, 0L, occurredAt));
    }

    public void recordClose(Long tenantId, String paymentNo, String beforeStatus, String afterStatus,
                            java.time.Instant occurredAt) {
        saveSafely(new PaymentAuditLog(null, tenantId, paymentNo, PaymentAuditLog.ACTION_CLOSE, beforeStatus,
                afterStatus, PaymentAuditLog.OPERATOR_SYSTEM, 0L, occurredAt));
    }
}
