package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditLog;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditLogRepository;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 存储审计应用服务。
 */
@Service
public class StorageAuditApplicationService {

    private static final Logger log = LoggerFactory.getLogger(StorageAuditApplicationService.class);

    private final StorageAuditLogRepository storageAuditLogRepository;

    public StorageAuditApplicationService(StorageAuditLogRepository storageAuditLogRepository) {
        this.storageAuditLogRepository = storageAuditLogRepository;
    }

    public void record(String tenantId, Long objectId, String ownerType, String ownerId, String actionType,
                       String beforeStatus, String afterStatus) {
        try {
            storageAuditLogRepository.save(StorageAuditLog.systemAction(tenantId, objectId, ownerType, ownerId,
                    actionType, beforeStatus, afterStatus));
            Metrics.counter("bacon.storage.audit.write.success.total", "actionType", actionType).increment();
        } catch (RuntimeException ex) {
            Metrics.counter("bacon.storage.audit.write.fail.total", "actionType", actionType).increment();
            log.error("ALERT storage audit log write failed, objectId={}, ownerType={}, ownerId={}, actionType={}",
                    objectId, ownerType, ownerId, actionType, ex);
        }
    }
}
