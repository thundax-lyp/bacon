package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.common.core.exception.ConflictException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.api.enums.ObjectStatusEnum;
import com.github.thundax.bacon.storage.api.enums.ReferenceStatusEnum;
import com.github.thundax.bacon.storage.application.support.StorageAuditApplicationService;
import com.github.thundax.bacon.storage.domain.model.entity.StorageAuditLog;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObjectReference;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectStorageResult;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectReferenceRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 存储对象命令应用服务。
 */
@Service
public class StoredObjectApplicationService {

    private final StoredObjectRepository storedObjectRepository;
    private final StoredObjectReferenceRepository storedObjectReferenceRepository;
    private final StoredObjectStorageRepository storedObjectStorageRepository;
    private final StorageAuditApplicationService storageAuditApplicationService;

    public StoredObjectApplicationService(StoredObjectRepository storedObjectRepository,
                                          StoredObjectReferenceRepository storedObjectReferenceRepository,
                                          StoredObjectStorageRepository storedObjectStorageRepository,
                                          StorageAuditApplicationService storageAuditApplicationService) {
        this.storedObjectRepository = storedObjectRepository;
        this.storedObjectReferenceRepository = storedObjectReferenceRepository;
        this.storedObjectStorageRepository = storedObjectStorageRepository;
        this.storageAuditApplicationService = storageAuditApplicationService;
    }

    @Transactional
    public StoredObjectDTO uploadObject(UploadObjectCommand command) {
        StoredObjectStorageResult storageResult = storedObjectStorageRepository.upload(command.getCategory(),
                command.getOriginalFilename(), command.getContentType(), command.getInputStream());
        StoredObject storedObject = new StoredObject(null, command.getTenantId(), storageResult.getStorageType(),
                storageResult.getBucketName(), storageResult.getObjectKey(), command.getOriginalFilename(),
                command.getContentType(), command.getSize(), storageResult.getAccessUrl(), ObjectStatusEnum.ACTIVE.name(),
                ReferenceStatusEnum.UNREFERENCED.name(), null, Instant.now(), null, Instant.now());
        StoredObject savedObject = storedObjectRepository.save(storedObject);
        storageAuditApplicationService.record(savedObject.getTenantId(), savedObject.getId(), command.getOwnerType(), null,
                StorageAuditLog.ACTION_UPLOAD, null, savedObject.getObjectStatus());
        return toDto(savedObject);
    }

    @Transactional
    public void markObjectReferenced(Long objectId, String ownerType, String ownerId) {
        StoredObject storedObject = storedObjectRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + objectId));
        String beforeStatus = storedObject.getReferenceStatus();
        if (!storedObjectReferenceRepository.existsByObjectIdAndOwner(objectId, ownerType, ownerId)) {
            StoredObjectReference reference = new StoredObjectReference(null, objectId, ownerType, ownerId);
            storedObjectReferenceRepository.save(reference);
        }
        storedObject.markReferenced();
        StoredObject savedObject = storedObjectRepository.save(storedObject);
        storageAuditApplicationService.record(savedObject.getTenantId(), objectId, ownerType, ownerId,
                StorageAuditLog.ACTION_REFERENCE_ADD, beforeStatus, savedObject.getReferenceStatus());
    }

    @Transactional
    public void clearObjectReference(Long objectId, String ownerType, String ownerId) {
        StoredObject storedObject = storedObjectRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + objectId));
        String beforeStatus = storedObject.getReferenceStatus();
        storedObjectReferenceRepository.deleteByObjectIdAndOwner(objectId, ownerType, ownerId);
        if (!storedObjectReferenceRepository.existsByObjectId(objectId)) {
            storedObject.markUnreferenced();
            storedObject = storedObjectRepository.save(storedObject);
        }
        storageAuditApplicationService.record(storedObject.getTenantId(), objectId, ownerType, ownerId,
                StorageAuditLog.ACTION_REFERENCE_CLEAR, beforeStatus, storedObject.getReferenceStatus());
    }

    @Transactional
    public void deleteObject(Long objectId) {
        StoredObject storedObject = storedObjectRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + objectId));
        if (storedObjectReferenceRepository.existsByObjectId(objectId)) {
            throw new ConflictException("Stored object is still referenced: " + objectId);
        }
        String beforeStatus = storedObject.getObjectStatus();
        storedObjectStorageRepository.delete(storedObject);
        storedObject.markDeleted();
        StoredObject savedObject = storedObjectRepository.save(storedObject);
        storageAuditApplicationService.record(savedObject.getTenantId(), objectId, null, null,
                StorageAuditLog.ACTION_DELETE, beforeStatus, savedObject.getObjectStatus());
    }

    private StoredObjectDTO toDto(StoredObject storedObject) {
        return new StoredObjectDTO(storedObject.getId(), storedObject.getStorageType(), storedObject.getBucketName(),
                storedObject.getObjectKey(), storedObject.getOriginalFilename(), storedObject.getContentType(),
                storedObject.getSize(), storedObject.getAccessUrl(), storedObject.getObjectStatus(),
                storedObject.getReferenceStatus(), storedObject.getCreatedAt());
    }
}
