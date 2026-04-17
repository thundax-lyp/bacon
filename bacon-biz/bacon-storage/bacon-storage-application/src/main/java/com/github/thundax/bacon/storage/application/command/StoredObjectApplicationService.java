package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.TimestampedBizCodeFormatter;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.application.assembler.StoredObjectAssembler;
import com.github.thundax.bacon.storage.application.support.StorageAuditApplicationService;
import com.github.thundax.bacon.storage.application.support.StorageUploadLimitValidator;
import com.github.thundax.bacon.storage.application.support.StoredObjectDeletionTransactionService;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObjectReference;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditActionType;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectNo;
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

    private static final String STORED_OBJECT_ID_BIZ_TAG = "stored-object-id";
    private static final String STORED_OBJECT_NO_BIZ_TAG = "stored-object-no";
    private static final String STORED_OBJECT_NO_DOMAIN = "storage-";

    private final StoredObjectRepository storedObjectRepository;
    private final StoredObjectReferenceRepository storedObjectReferenceRepository;
    private final StoredObjectStorageRepository storedObjectStorageRepository;
    private final StorageAuditApplicationService storageAuditApplicationService;
    private final StoredObjectDeletionTransactionService storedObjectDeletionTransactionService;
    private final StorageUploadLimitValidator storageUploadLimitValidator;
    private final IdGenerator idGenerator;

    public StoredObjectApplicationService(
            StoredObjectRepository storedObjectRepository,
            StoredObjectReferenceRepository storedObjectReferenceRepository,
            StoredObjectStorageRepository storedObjectStorageRepository,
            StorageAuditApplicationService storageAuditApplicationService,
            StoredObjectDeletionTransactionService storedObjectDeletionTransactionService,
            StorageUploadLimitValidator storageUploadLimitValidator,
            IdGenerator idGenerator) {
        this.storedObjectRepository = storedObjectRepository;
        this.storedObjectReferenceRepository = storedObjectReferenceRepository;
        this.storedObjectStorageRepository = storedObjectStorageRepository;
        this.storageAuditApplicationService = storageAuditApplicationService;
        this.storedObjectDeletionTransactionService = storedObjectDeletionTransactionService;
        this.storageUploadLimitValidator = storageUploadLimitValidator;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public StoredObjectDTO uploadObject(UploadObjectCommand command) {
        storageUploadLimitValidator.validateSingleUpload(command.getSize());
        StoredObjectStorageResult storageResult = storedObjectStorageRepository.upload(
                command.getCategory(),
                command.getOriginalFilename(),
                command.getContentType(),
                command.getInputStream());
        long storedObjectNoSeed = idGenerator.nextId(STORED_OBJECT_NO_BIZ_TAG);
        StoredObject storedObject = StoredObject.create(
                StoredObjectId.of(idGenerator.nextId(STORED_OBJECT_ID_BIZ_TAG)),
                StoredObjectNo.of(TimestampedBizCodeFormatter.format(STORED_OBJECT_NO_DOMAIN, storedObjectNoSeed)),
                storageResult.storageType(),
                storageResult.bucketName(),
                storageResult.objectKey(),
                command.getOriginalFilename(),
                command.getContentType(),
                command.getSize(),
                storageResult.accessEndpoint());
        StoredObject savedObject = storedObjectRepository.insert(storedObject);
        storageAuditApplicationService.record(
                null,
                savedObject.getId(),
                command.getOwnerType(),
                null,
                StorageAuditActionType.UPLOAD,
                null,
                savedObject.getObjectStatus().value());
        return StoredObjectAssembler.toDto(savedObject);
    }

    @Transactional
    public void markObjectReferenced(String storedObjectNo, String ownerType, String ownerId) {
        StoredObjectNo objectNo = StoredObjectNo.of(storedObjectNo);
        StoredObject storedObject = storedObjectRepository
                .findByNo(objectNo)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + storedObjectNo));
        StoredObjectId storedObjectId = storedObject.getId();
        ensureAvailable(storedObject, storedObjectNo);
        String beforeStatus = storedObject.getReferenceStatus().value();
        StoredObjectReference reference = StoredObjectReference.create(storedObjectId, ownerType, ownerId);
        boolean created = storedObjectReferenceRepository.saveIfAbsent(reference);
        StoredObject savedObject =
                syncReferenceStatus(storedObject, storedObjectReferenceRepository.existsByObjectId(storedObjectId));
        if (!created) {
            return;
        }
        storageAuditApplicationService.record(
                null,
                storedObjectId,
                ownerType,
                ownerId,
                StorageAuditActionType.REFERENCE_ADD,
                beforeStatus,
                savedObject.getReferenceStatus().value());
    }

    @Transactional
    public void clearObjectReference(String storedObjectNo, String ownerType, String ownerId) {
        StoredObjectNo objectNo = StoredObjectNo.of(storedObjectNo);
        StoredObject storedObject = storedObjectRepository
                .findByNo(objectNo)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + storedObjectNo));
        StoredObjectId storedObjectId = storedObject.getId();
        String beforeStatus = storedObject.getReferenceStatus().value();
        boolean deleted = storedObjectReferenceRepository.deleteByObjectIdAndOwner(storedObjectId, ownerType, ownerId);
        StoredObject savedObject =
                syncReferenceStatus(storedObject, storedObjectReferenceRepository.existsByObjectId(storedObjectId));
        if (!deleted) {
            return;
        }
        storageAuditApplicationService.record(
                null,
                storedObjectId,
                ownerType,
                ownerId,
                StorageAuditActionType.REFERENCE_CLEAR,
                beforeStatus,
                savedObject.getReferenceStatus().value());
    }

    public void deleteObject(String storedObjectNo) {
        StoredObjectNo objectNo = StoredObjectNo.of(storedObjectNo);
        StoredObjectId storedObjectId = storedObjectRepository
                .findByNo(objectNo)
                .map(StoredObject::getId)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + storedObjectNo));
        StoredObject storedObject = storedObjectDeletionTransactionService.markDeleting(storedObjectId);
        if (storedObject.isDeleted()) {
            return;
        }
        try {
            storedObjectStorageRepository.delete(storedObject);
            storedObjectDeletionTransactionService.markDeleted(storedObjectId);
        } catch (RuntimeException ex) {
            log.warn(
                    "Stored object physical delete failed, objectId={}, objectKey={}, storageType={}",
                    storedObject.getId(),
                    storedObject.getObjectKey(),
                    storedObject.getStorageType(),
                    ex);
            throw ex;
        }
    }

    private void ensureAvailable(StoredObject storedObject, String storedObjectNo) {
        if (storedObject.isDeleting() || storedObject.isDeleted()) {
            throw new NotFoundException("Stored object is unavailable: " + storedObjectNo);
        }
    }

    private StoredObject syncReferenceStatus(StoredObject storedObject, boolean referenced) {
        if (referenced && !storedObject.isReferenced()) {
            storedObject.markReferenced();
            return storedObjectRepository.update(storedObject);
        }
        if (!referenced && storedObject.isReferenced()) {
            storedObject.markUnreferenced();
            return storedObjectRepository.update(storedObject);
        }
        return storedObject;
    }
}
