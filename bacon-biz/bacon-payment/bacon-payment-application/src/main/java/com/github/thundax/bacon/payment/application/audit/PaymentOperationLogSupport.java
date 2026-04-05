package com.github.thundax.bacon.payment.application.audit;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditActionType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditOperatorType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.domain.model.valueobject.PaymentNo;
import com.github.thundax.bacon.payment.domain.repository.PaymentAuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentOperationLogSupport {

    private final PaymentAuditLogRepository paymentAuditLogRepository;

    public PaymentOperationLogSupport(PaymentAuditLogRepository paymentAuditLogRepository) {
        this.paymentAuditLogRepository = paymentAuditLogRepository;
    }

    public void saveSafely(PaymentAuditLog auditLog) {
        try {
            paymentAuditLogRepository.save(auditLog);
        } catch (RuntimeException ex) {
            log.error("ALERT payment audit write failed, tenantId={}, paymentNo={}, actionType={}",
                    auditLog.getTenantId(), auditLog.getPaymentNo().value(), auditLog.getActionType().value(), ex);
        }
    }

    public void recordCreate(Long tenantId, String paymentNo, String afterStatus, java.time.Instant occurredAt) {
        saveSafely(new PaymentAuditLog(null, toTenantId(tenantId), PaymentNo.of(paymentNo), PaymentAuditActionType.CREATE,
                null, PaymentStatus.fromValue(afterStatus), PaymentAuditOperatorType.SYSTEM, "0", occurredAt));
    }

    public void recordCallback(PaymentAuditActionType actionType, Long tenantId, String paymentNo, String beforeStatus,
                               String afterStatus, java.time.Instant occurredAt) {
        saveSafely(new PaymentAuditLog(null, toTenantId(tenantId), PaymentNo.of(paymentNo),
                actionType, PaymentStatus.fromValue(beforeStatus),
                PaymentStatus.fromValue(afterStatus), PaymentAuditOperatorType.CHANNEL, "0", occurredAt));
    }

    public void recordClose(Long tenantId, String paymentNo, String beforeStatus, String afterStatus,
                            java.time.Instant occurredAt) {
        saveSafely(new PaymentAuditLog(null, toTenantId(tenantId), PaymentNo.of(paymentNo), PaymentAuditActionType.CLOSE,
                PaymentStatus.fromValue(beforeStatus), PaymentStatus.fromValue(afterStatus), PaymentAuditOperatorType.SYSTEM,
                "0", occurredAt));
    }

    private TenantId toTenantId(Long tenantId) {
        return tenantId == null ? null : TenantId.of(tenantId);
    }
}
