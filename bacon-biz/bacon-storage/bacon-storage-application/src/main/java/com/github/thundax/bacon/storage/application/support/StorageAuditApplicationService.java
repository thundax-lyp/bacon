package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditLog;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditOutbox;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditActionType;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditOutboxStatus;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditLogRepository;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditOutboxRepository;
import io.micrometer.core.instrument.Metrics;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 存储审计应用服务。
 */
@Service
public class StorageAuditApplicationService {

    private static final String AUDIT_LOG_BIZ_TAG = "storage_audit_log";
    private static final Logger log = LoggerFactory.getLogger(StorageAuditApplicationService.class);

    private final IdGenerator idGenerator;
    private final StorageAuditLogRepository storageAuditLogRepository;
    private final StorageAuditOutboxRepository storageAuditOutboxRepository;

    public StorageAuditApplicationService(
            IdGenerator idGenerator,
            StorageAuditLogRepository storageAuditLogRepository,
            StorageAuditOutboxRepository storageAuditOutboxRepository) {
        this.idGenerator = idGenerator;
        this.storageAuditLogRepository = storageAuditLogRepository;
        this.storageAuditOutboxRepository = storageAuditOutboxRepository;
    }

    public void record(
            TenantId tenantId,
            StoredObjectId objectId,
            String ownerType,
            String ownerId,
            StorageAuditActionType actionType,
            String beforeStatus,
            String afterStatus) {
        StorageAuditLog auditLog = StorageAuditLog.create(
                idGenerator.nextId(AUDIT_LOG_BIZ_TAG),
                objectId,
                ownerType,
                ownerId,
                actionType,
                beforeStatus,
                afterStatus,
                StorageAuditLog.OPERATOR_TYPE_SYSTEM,
                StorageAuditLog.OPERATOR_ID_SYSTEM,
                Instant.now());
        try {
            storageAuditLogRepository.insert(auditLog);
            Metrics.counter("bacon.storage.audit.write.success.total", "actionType", actionType.value())
                    .increment();
        } catch (RuntimeException ex) {
            Metrics.counter("bacon.storage.audit.write.fail.total", "actionType", actionType.value())
                    .increment();
            saveAuditOutboxSafely(auditLog, ex);
            log.error(
                    "ALERT storage audit log write failed, objectId={}, ownerType={}, ownerId={}, actionType={}",
                    objectId,
                    ownerType,
                    ownerId,
                    actionType.value(),
                    ex);
        }
    }

    private void saveAuditOutboxSafely(StorageAuditLog auditLog, RuntimeException ex) {
        try {
            storageAuditOutboxRepository.insert(
                    StorageAuditOutbox.create(
                            null,
                            auditLog.getObjectId(),
                            auditLog.getOwnerType(),
                            auditLog.getOwnerId(),
                            auditLog.getActionType(),
                            auditLog.getBeforeStatus(),
                            auditLog.getAfterStatus(),
                            auditLog.getOperatorType(),
                            auditLog.getOperatorId(),
                            auditLog.getOccurredAt(),
                            truncateMessage(ex.getMessage()),
                            StorageAuditOutboxStatus.NEW,
                            0,
                            Instant.now(),
                            Instant.now()));
            Metrics.counter(
                            "bacon.storage.audit.outbox.persist.success.total",
                            "actionType",
                            auditLog.getActionType().value())
                    .increment();
        } catch (RuntimeException outboxEx) {
            Metrics.counter(
                            "bacon.storage.audit.outbox.persist.fail.total",
                            "actionType",
                            auditLog.getActionType().value())
                    .increment();
            log.error(
                    "ALERT storage audit outbox persist failed, objectId={}, ownerType={}, ownerId={}, actionType={}",
                    auditLog.getObjectId(),
                    auditLog.getOwnerType(),
                    auditLog.getOwnerId(),
                    auditLog.getActionType().value(),
                    outboxEx);
        }
    }

    private String truncateMessage(String message) {
        if (message == null || message.isBlank()) {
            return "UNKNOWN";
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }
}
