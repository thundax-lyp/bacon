package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditLog;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditOutbox;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditLogRepository;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditOutboxRepository;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * 存储审计应用服务。
 */
@Service
public class StorageAuditApplicationService {

    private static final Logger log = LoggerFactory.getLogger(StorageAuditApplicationService.class);

    private final StorageAuditLogRepository storageAuditLogRepository;
    private final StorageAuditOutboxRepository storageAuditOutboxRepository;

    public StorageAuditApplicationService(StorageAuditLogRepository storageAuditLogRepository,
                                          StorageAuditOutboxRepository storageAuditOutboxRepository) {
        this.storageAuditLogRepository = storageAuditLogRepository;
        this.storageAuditOutboxRepository = storageAuditOutboxRepository;
    }

    public void record(String tenantId, Long objectId, String ownerType, String ownerId, String actionType,
                       String beforeStatus, String afterStatus) {
        StorageAuditLog auditLog = StorageAuditLog.systemAction(tenantId, objectId, ownerType, ownerId,
                actionType, beforeStatus, afterStatus);
        try {
            storageAuditLogRepository.save(auditLog);
            Metrics.counter("bacon.storage.audit.write.success.total", "actionType", actionType).increment();
        } catch (RuntimeException ex) {
            Metrics.counter("bacon.storage.audit.write.fail.total", "actionType", actionType).increment();
            saveAuditOutboxSafely(auditLog, ex);
            log.error("ALERT storage audit log write failed, objectId={}, ownerType={}, ownerId={}, actionType={}",
                    objectId, ownerType, ownerId, actionType, ex);
        }
    }

    private void saveAuditOutboxSafely(StorageAuditLog auditLog, RuntimeException ex) {
        try {
            storageAuditOutboxRepository.save(StorageAuditOutbox.newEvent(auditLog, truncateMessage(ex.getMessage()), Instant.now()));
            Metrics.counter("bacon.storage.audit.outbox.persist.success.total", "actionType", auditLog.getActionType())
                    .increment();
        } catch (RuntimeException outboxEx) {
            Metrics.counter("bacon.storage.audit.outbox.persist.fail.total", "actionType", auditLog.getActionType())
                    .increment();
            log.error("ALERT storage audit outbox persist failed, objectId={}, ownerType={}, ownerId={}, actionType={}",
                    auditLog.getObjectId(), auditLog.getOwnerType(), auditLog.getOwnerId(), auditLog.getActionType(),
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
