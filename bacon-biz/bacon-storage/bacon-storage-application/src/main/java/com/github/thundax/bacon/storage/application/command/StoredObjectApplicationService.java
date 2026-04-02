package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.application.support.StoredObjectDeletionTransactionService;
import com.github.thundax.bacon.storage.application.support.StorageAuditApplicationService;
import com.github.thundax.bacon.storage.application.support.StorageUploadLimitValidator;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditLog;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObjectReference;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectStorageResult;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectReferenceRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 存储对象命令应用服务。
 */
@Slf4j
@Service
public class StoredObjectApplicationService {

    private final StoredObjectRepository storedObjectRepository;
    private final StoredObjectReferenceRepository storedObjectReferenceRepository;
    private final StoredObjectStorageRepository storedObjectStorageRepository;
    private final StorageAuditApplicationService storageAuditApplicationService;
    private final StoredObjectDeletionTransactionService storedObjectDeletionTransactionService;
    private final StorageUploadLimitValidator storageUploadLimitValidator;

    public StoredObjectApplicationService(StoredObjectRepository storedObjectRepository,
                                          StoredObjectReferenceRepository storedObjectReferenceRepository,
                                          StoredObjectStorageRepository storedObjectStorageRepository,
                                          StorageAuditApplicationService storageAuditApplicationService,
                                          StoredObjectDeletionTransactionService storedObjectDeletionTransactionService,
                                          StorageUploadLimitValidator storageUploadLimitValidator) {
        this.storedObjectRepository = storedObjectRepository;
        this.storedObjectReferenceRepository = storedObjectReferenceRepository;
        this.storedObjectStorageRepository = storedObjectStorageRepository;
        this.storageAuditApplicationService = storageAuditApplicationService;
        this.storedObjectDeletionTransactionService = storedObjectDeletionTransactionService;
        this.storageUploadLimitValidator = storageUploadLimitValidator;
    }

    @Transactional
    public StoredObjectDTO uploadObject(UploadObjectCommand command) {
        storageUploadLimitValidator.validateSingleUpload(command.getSize());
        StoredObjectStorageResult storageResult = storedObjectStorageRepository.upload(command.getCategory(),
                command.getOriginalFilename(), command.getContentType(), command.getInputStream());
        StoredObject storedObject = StoredObject.newUploadedObject(toTenantId(command.getTenantId()), storageResult.getStorageType(),
                storageResult.getBucketName(), storageResult.getObjectKey(), command.getOriginalFilename(),
                command.getContentType(), command.getSize(), storageResult.getAccessEndpoint(), null);
        StoredObject savedObject = storedObjectRepository.save(storedObject);
        storageAuditApplicationService.record(savedObject.getTenantId(), savedObject.getId(), command.getOwnerType(), null,
                StorageAuditLog.ACTION_UPLOAD, null, savedObject.getObjectStatus());
        return toDto(savedObject);
    }

    @Transactional
    public void markObjectReferenced(String objectId, String ownerType, String ownerId) {
        StoredObjectId storedObjectId = StoredObjectId.of(objectId);
        StoredObject storedObject = storedObjectRepository.findById(storedObjectId)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + objectId));
        ensureAvailable(storedObject, objectId);
        String beforeStatus = storedObject.getReferenceStatus();
        StoredObjectReference reference = StoredObjectReference.create(storedObjectId, ownerType, ownerId);
        boolean created = storedObjectReferenceRepository.saveIfAbsent(reference);
        StoredObject savedObject = syncReferenceStatus(storedObject, storedObjectReferenceRepository.existsByObjectId(storedObjectId));
        if (!created) {
            return;
        }
        storageAuditApplicationService.record(savedObject.getTenantId(), storedObjectId, ownerType, ownerId,
                StorageAuditLog.ACTION_REFERENCE_ADD, beforeStatus, savedObject.getReferenceStatus());
    }

    @Transactional
    public void clearObjectReference(String objectId, String ownerType, String ownerId) {
        StoredObjectId storedObjectId = StoredObjectId.of(objectId);
        StoredObject storedObject = storedObjectRepository.findById(storedObjectId)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + objectId));
        String beforeStatus = storedObject.getReferenceStatus();
        boolean deleted = storedObjectReferenceRepository.deleteByObjectIdAndOwner(storedObjectId, ownerType, ownerId);
        StoredObject savedObject = syncReferenceStatus(storedObject, storedObjectReferenceRepository.existsByObjectId(storedObjectId));
        if (!deleted) {
            return;
        }
        storageAuditApplicationService.record(savedObject.getTenantId(), storedObjectId, ownerType, ownerId,
                StorageAuditLog.ACTION_REFERENCE_CLEAR, beforeStatus, savedObject.getReferenceStatus());
    }

    public void deleteObject(String objectId) {
        StoredObjectId storedObjectId = StoredObjectId.of(objectId);
        StoredObject storedObject = storedObjectDeletionTransactionService.markDeleting(storedObjectId);
        if (storedObject.isDeleted()) {
            return;
        }
        try {
            storedObjectStorageRepository.delete(storedObject);
            storedObjectDeletionTransactionService.markDeleted(storedObjectId);
        } catch (RuntimeException ex) {
            log.warn("Stored object physical delete failed, objectId={}, objectKey={}, storageType={}",
                    storedObject.getId(), storedObject.getObjectKey(), storedObject.getStorageType(), ex);
            throw ex;
        }
    }

    private StoredObjectDTO toDto(StoredObject storedObject) {
        return new StoredObjectDTO(storedObject.getId() == null ? null : storedObject.getId().value(),
                storedObject.getStorageType() == null ? null : storedObject.getStorageType().value(),
                storedObject.getBucketName(),
                storedObject.getObjectKey(), storedObject.getOriginalFilename(), storedObject.getContentType(),
                storedObject.getSize(), storedObject.getAccessEndpoint(), storedObject.getObjectStatus(),
                storedObject.getReferenceStatus(), storedObject.getCreatedAt());
    }

    private void ensureAvailable(StoredObject storedObject, String objectId) {
        if (storedObject.isDeleting() || storedObject.isDeleted()) {
            throw new NotFoundException("Stored object is unavailable: " + objectId);
        }
    }

    private StoredObject syncReferenceStatus(StoredObject storedObject, boolean referenced) {
        if (referenced && !storedObject.isReferenced()) {
            storedObject.markReferenced();
            return storedObjectRepository.save(storedObject);
        }
        if (!referenced && storedObject.isReferenced()) {
            storedObject.markUnreferenced();
            return storedObjectRepository.save(storedObject);
        }
        return storedObject;
    }

    private TenantId toTenantId(String tenantId) {
        return tenantId == null || tenantId.isBlank() ? null : TenantId.of(tenantId);
    }
}
