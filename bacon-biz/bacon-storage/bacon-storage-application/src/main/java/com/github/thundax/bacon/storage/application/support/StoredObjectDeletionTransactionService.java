package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.common.core.exception.ConflictException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditLog;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectReferenceRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 存储对象删除事务切面服务。
 */
@Service
public class StoredObjectDeletionTransactionService {

    private final StoredObjectRepository storedObjectRepository;
    private final StoredObjectReferenceRepository storedObjectReferenceRepository;
    private final StorageAuditApplicationService storageAuditApplicationService;

    public StoredObjectDeletionTransactionService(StoredObjectRepository storedObjectRepository,
                                                 StoredObjectReferenceRepository storedObjectReferenceRepository,
                                                 StorageAuditApplicationService storageAuditApplicationService) {
        this.storedObjectRepository = storedObjectRepository;
        this.storedObjectReferenceRepository = storedObjectReferenceRepository;
        this.storageAuditApplicationService = storageAuditApplicationService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StoredObject markDeleting(StoredObjectId objectId) {
        StoredObject storedObject = storedObjectRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + objectId));
        if (storedObject.isDeleted() || storedObject.isDeleting()) {
            return storedObject;
        }
        if (storedObjectReferenceRepository.existsByObjectId(objectId)) {
            throw new ConflictException("Stored object is still referenced: " + objectId);
        }
        storedObject.markDeleting();
        return storedObjectRepository.save(storedObject);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markDeleted(StoredObjectId objectId) {
        StoredObject storedObject = storedObjectRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + objectId));
        if (storedObject.isDeleted()) {
            return;
        }
        String beforeStatus = storedObject.getObjectStatus().value();
        storedObject.markDeleted();
        StoredObject savedObject = storedObjectRepository.save(storedObject);
        storageAuditApplicationService.record(savedObject.getTenantId(), objectId, null, null,
                StorageAuditLog.ACTION_DELETE, beforeStatus, savedObject.getObjectStatus().value());
    }
}
