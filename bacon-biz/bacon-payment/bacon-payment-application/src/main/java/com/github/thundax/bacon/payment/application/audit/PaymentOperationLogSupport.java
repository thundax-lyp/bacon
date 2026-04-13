package com.github.thundax.bacon.payment.application.audit;

import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditActionType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentAuditOperatorType;
import com.github.thundax.bacon.payment.domain.model.enums.PaymentStatus;
import com.github.thundax.bacon.payment.domain.repository.PaymentAuditLogRepository;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentOperationLogSupport {

    private static final String PAYMENT_AUDIT_LOG_ID_BIZ_TAG = "payment_audit_log_id";

    private final PaymentAuditLogRepository paymentAuditLogRepository;
    private final IdGenerator idGenerator;

    public PaymentOperationLogSupport(PaymentAuditLogRepository paymentAuditLogRepository, IdGenerator idGenerator) {
        this.paymentAuditLogRepository = paymentAuditLogRepository;
        this.idGenerator = idGenerator;
    }

    public void saveSafely(PaymentAuditLog auditLog) {
        try {
            paymentAuditLogRepository.save(auditLog);
        } catch (RuntimeException ex) {
            log.error(
                    "ALERT payment audit write failed, paymentNo={}, actionType={}",
                    auditLog.getPaymentNo().value(),
                    auditLog.getActionType().value(),
                    ex);
        }
    }

    public void recordCreate(String paymentNo, String afterStatus, Instant occurredAt) {
        BaconContextHolder.requireTenantId();
        saveSafely(PaymentAuditLog.create(
                nextAuditLogId(),
                PaymentNo.of(paymentNo),
                PaymentAuditActionType.CREATE,
                null,
                PaymentStatus.fromValue(afterStatus),
                PaymentAuditOperatorType.SYSTEM,
                "0",
                occurredAt));
    }

    public void recordCallback(
            PaymentAuditActionType actionType,
            String paymentNo,
            String beforeStatus,
            String afterStatus,
            Instant occurredAt) {
        BaconContextHolder.requireTenantId();
        saveSafely(PaymentAuditLog.create(
                nextAuditLogId(),
                PaymentNo.of(paymentNo),
                actionType,
                PaymentStatus.fromValue(beforeStatus),
                PaymentStatus.fromValue(afterStatus),
                PaymentAuditOperatorType.CHANNEL,
                "0",
                occurredAt));
    }

    public void recordClose(String paymentNo, String beforeStatus, String afterStatus, Instant occurredAt) {
        BaconContextHolder.requireTenantId();
        saveSafely(PaymentAuditLog.create(
                nextAuditLogId(),
                PaymentNo.of(paymentNo),
                PaymentAuditActionType.CLOSE,
                PaymentStatus.fromValue(beforeStatus),
                PaymentStatus.fromValue(afterStatus),
                PaymentAuditOperatorType.SYSTEM,
                "0",
                occurredAt));
    }

    private Long nextAuditLogId() {
        return idGenerator.nextId(PAYMENT_AUDIT_LOG_ID_BIZ_TAG);
    }
}
