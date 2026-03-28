package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.common.core.exception.ConflictException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.application.support.StoredObjectDeletionTransactionService;
import com.github.thundax.bacon.storage.application.support.StorageAuditApplicationService;
import com.github.thundax.bacon.storage.application.support.StorageUploadLimitValidator;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditLog;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObjectReference;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectStorageResult;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectReferenceRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 存储对象命令应用服务。
 */
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
        StoredObject storedObject = StoredObject.newUploadedObject(command.getTenantId(), storageResult.getStorageType(),
                storageResult.getBucketName(), storageResult.getObjectKey(), command.getOriginalFilename(),
                command.getContentType(), command.getSize(), storageResult.getAccessEndpoint(), null);
        StoredObject savedObject = storedObjectRepository.save(storedObject);
        storageAuditApplicationService.record(savedObject.getTenantId(), savedObject.getId(), command.getOwnerType(), null,
                StorageAuditLog.ACTION_UPLOAD, null, savedObject.getObjectStatus());
        return toDto(savedObject);
    }

    @Transactional
    public void markObjectReferenced(Long objectId, String ownerType, String ownerId) {
        StoredObject storedObject = storedObjectRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + objectId));
        ensureAvailable(storedObject, objectId);
        if (storedObjectReferenceRepository.existsByObjectIdAndOwner(objectId, ownerType, ownerId)) {
            return;
        }
        String beforeStatus = storedObject.getReferenceStatus();
        StoredObjectReference reference = StoredObjectReference.create(objectId, ownerType, ownerId);
        storedObjectReferenceRepository.save(reference);
        storedObject.markReferenced();
        StoredObject savedObject = storedObjectRepository.save(storedObject);
        storageAuditApplicationService.record(savedObject.getTenantId(), objectId, ownerType, ownerId,
                StorageAuditLog.ACTION_REFERENCE_ADD, beforeStatus, savedObject.getReferenceStatus());
    }

    @Transactional
    public void clearObjectReference(Long objectId, String ownerType, String ownerId) {
        StoredObject storedObject = storedObjectRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + objectId));
        if (!storedObjectReferenceRepository.existsByObjectIdAndOwner(objectId, ownerType, ownerId)) {
            return;
        }
        String beforeStatus = storedObject.getReferenceStatus();
        storedObjectReferenceRepository.deleteByObjectIdAndOwner(objectId, ownerType, ownerId);
        if (!storedObjectReferenceRepository.existsByObjectId(objectId)) {
            storedObject.markUnreferenced();
            storedObject = storedObjectRepository.save(storedObject);
        }
        storageAuditApplicationService.record(storedObject.getTenantId(), objectId, ownerType, ownerId,
                StorageAuditLog.ACTION_REFERENCE_CLEAR, beforeStatus, storedObject.getReferenceStatus());
    }

    public void deleteObject(Long objectId) {
        StoredObject storedObject = storedObjectDeletionTransactionService.markDeleting(objectId);
        if (storedObject.isDeleted()) {
            return;
        }
        storedObjectStorageRepository.delete(storedObject);
        storedObjectDeletionTransactionService.markDeleted(objectId);
    }

    private StoredObjectDTO toDto(StoredObject storedObject) {
        return new StoredObjectDTO(storedObject.getId(), storedObject.getStorageType(), storedObject.getBucketName(),
                storedObject.getObjectKey(), storedObject.getOriginalFilename(), storedObject.getContentType(),
                storedObject.getSize(), storedObject.getAccessEndpoint(), storedObject.getObjectStatus(),
                storedObject.getReferenceStatus(), storedObject.getCreatedAt());
    }

    private void ensureAvailable(StoredObject storedObject, Long objectId) {
        if (storedObject.isDeleting() || storedObject.isDeleted()) {
            throw new NotFoundException("Stored object is unavailable: " + objectId);
        }
    }
}
