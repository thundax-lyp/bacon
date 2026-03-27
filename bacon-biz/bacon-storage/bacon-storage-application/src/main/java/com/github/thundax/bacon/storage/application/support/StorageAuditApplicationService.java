package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditLog;
import com.github.thundax.bacon.storage.domain.repository.StorageAuditLogRepository;
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

    public StorageAuditApplicationService(StorageAuditLogRepository storageAuditLogRepository) {
        this.storageAuditLogRepository = storageAuditLogRepository;
    }

    public void record(String tenantId, Long objectId, String ownerType, String ownerId, String actionType,
                       String beforeStatus, String afterStatus) {
        try {
            storageAuditLogRepository.save(new StorageAuditLog(null, tenantId, objectId, ownerType, ownerId, actionType,
                    beforeStatus, afterStatus, StorageAuditLog.OPERATOR_TYPE_SYSTEM,
                    StorageAuditLog.OPERATOR_ID_SYSTEM, Instant.now()));
        } catch (RuntimeException ex) {
            log.warn("Storage audit log write failed, objectId={}, actionType={}", objectId, actionType, ex);
        }
    }
}
