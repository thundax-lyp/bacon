package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.TimestampedBizCodeFormatter;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.application.assembler.MultipartUploadPartAssembler;
import com.github.thundax.bacon.storage.application.assembler.MultipartUploadSessionAssembler;
import com.github.thundax.bacon.storage.application.assembler.StoredObjectAssembler;
import com.github.thundax.bacon.storage.application.support.StorageAuditApplicationService;
import com.github.thundax.bacon.storage.application.support.StorageUploadLimitValidator;
import com.github.thundax.bacon.storage.application.support.StoredObjectDeletionTransactionService;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.application.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.application.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObjectReference;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditActionType;
import com.github.thundax.bacon.storage.domain.model.valueobject.MultipartUploadStorageSession;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectNo;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectStorageResult;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadPartRepository;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadSessionRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectReferenceRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 存储命令应用服务。
 */
@Slf4j
@Service
public class StoredObjectCommandApplicationService {

    private static final String UPLOAD_ID_DOMAIN = "storage";
    private static final String MULTIPART_UPLOAD_BIZ_TAG = "storage_multipart_upload";
    private static final String MULTIPART_UPLOAD_PART_BIZ_TAG = "storage_multipart_upload_part";
    private static final String STORED_OBJECT_ID_BIZ_TAG = "stored-object-id";
    private static final String STORED_OBJECT_NO_BIZ_TAG = "stored-object-no";
    private static final String STORED_OBJECT_NO_DOMAIN = "storage-";

    private final StoredObjectRepository storedObjectRepository;
    private final StoredObjectReferenceRepository storedObjectReferenceRepository;
    private final StoredObjectStorageRepository storedObjectStorageRepository;
    private final MultipartUploadSessionRepository multipartUploadSessionRepository;
    private final MultipartUploadPartRepository multipartUploadPartRepository;
    private final StorageAuditApplicationService storageAuditApplicationService;
    private final StoredObjectDeletionTransactionService storedObjectDeletionTransactionService;
    private final StorageUploadLimitValidator storageUploadLimitValidator;
    private final IdGenerator idGenerator;

    public StoredObjectCommandApplicationService(
            StoredObjectRepository storedObjectRepository,
            StoredObjectReferenceRepository storedObjectReferenceRepository,
            StoredObjectStorageRepository storedObjectStorageRepository,
            MultipartUploadSessionRepository multipartUploadSessionRepository,
            MultipartUploadPartRepository multipartUploadPartRepository,
            StorageAuditApplicationService storageAuditApplicationService,
            StoredObjectDeletionTransactionService storedObjectDeletionTransactionService,
            StorageUploadLimitValidator storageUploadLimitValidator,
            IdGenerator idGenerator) {
        this.storedObjectRepository = storedObjectRepository;
        this.storedObjectReferenceRepository = storedObjectReferenceRepository;
        this.storedObjectStorageRepository = storedObjectStorageRepository;
        this.multipartUploadSessionRepository = multipartUploadSessionRepository;
        this.multipartUploadPartRepository = multipartUploadPartRepository;
        this.storageAuditApplicationService = storageAuditApplicationService;
        this.storedObjectDeletionTransactionService = storedObjectDeletionTransactionService;
        this.storageUploadLimitValidator = storageUploadLimitValidator;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public StoredObjectDTO uploadObject(UploadObjectCommand command) {
        storageUploadLimitValidator.validateSingleUpload(command.size());
        StoredObjectStorageResult storageResult = storedObjectStorageRepository.insert(
                command.category(),
                command.originalFilename(),
                command.contentType(),
                command.inputStream());
        long storedObjectNoSeed = idGenerator.nextId(STORED_OBJECT_NO_BIZ_TAG);
        StoredObject storedObject = StoredObject.create(
                StoredObjectId.of(idGenerator.nextId(STORED_OBJECT_ID_BIZ_TAG)),
                StoredObjectNo.of(TimestampedBizCodeFormatter.format(STORED_OBJECT_NO_DOMAIN, storedObjectNoSeed)),
                storageResult.storageType(),
                storageResult.bucketName(),
                storageResult.objectKey(),
                command.originalFilename(),
                command.contentType(),
                command.size(),
                storageResult.accessEndpoint());
        StoredObject savedObject = storedObjectRepository.insert(storedObject);
        storageAuditApplicationService.record(
                null,
                savedObject.getId(),
                command.ownerType(),
                null,
                StorageAuditActionType.UPLOAD,
                null,
                savedObject.getObjectStatus().value());
        return StoredObjectAssembler.toDto(savedObject);
    }

    @Transactional
    public MultipartUploadSessionDTO initMultipartUpload(InitMultipartUploadCommand command) {
        storageUploadLimitValidator.validateMultipartInit(command.totalSize(), command.partSize());
        MultipartUploadStorageSession storageSession = storedObjectStorageRepository.insertMultipartUpload(
                command.category(), command.originalFilename(), command.contentType());
        Long sessionId = idGenerator.nextId(MULTIPART_UPLOAD_BIZ_TAG);
        String uploadId = TimestampedBizCodeFormatter.format(UPLOAD_ID_DOMAIN, sessionId);
        MultipartUploadSession session = MultipartUploadSession.create(
                sessionId,
                uploadId,
                command.ownerType(),
                command.ownerId(),
                command.category(),
                command.originalFilename(),
                command.contentType(),
                storageSession.objectKey(),
                storageSession.providerUploadId(),
                command.totalSize(),
                command.partSize(),
                Instant.now());
        MultipartUploadSession savedSession = multipartUploadSessionRepository.insert(session);
        return MultipartUploadSessionAssembler.toDto(savedSession);
    }

    @Transactional
    public MultipartUploadPartDTO uploadMultipartPart(UploadMultipartPartCommand command) {
        String uploadId = command.uploadId();
        MultipartUploadSession session = multipartUploadSessionRepository
                .findByUploadId(uploadId)
                .orElseThrow(
                        () -> new NotFoundException("Multipart upload session not found: " + command.uploadId()));
        session.assertOwnership(command.ownerType(), command.ownerId());
        storageUploadLimitValidator.validateMultipartPartUpload(session, command.size());
        boolean existingPartPresent = multipartUploadPartRepository
                .findByUploadIdAndPartNumber(uploadId, command.partNumber())
                .isPresent();
        String etag = storedObjectStorageRepository.insertPart(
                session, command.partNumber(), command.size(), command.inputStream());
        if (!existingPartPresent) {
            session.recordUploadedPart();
            multipartUploadSessionRepository.update(session);
        }
        MultipartUploadPart part = multipartUploadPartRepository
                .findByUploadIdAndPartNumber(uploadId, command.partNumber())
                .map(existingPart -> MultipartUploadPart.reconstruct(
                        existingPart.getId(),
                        existingPart.getUploadId(),
                        existingPart.getPartNumber(),
                        etag,
                        command.size(),
                        existingPart.getCreatedAt()))
                .orElseGet(() -> MultipartUploadPart.create(
                        idGenerator.nextId(MULTIPART_UPLOAD_PART_BIZ_TAG),
                        uploadId,
                        command.partNumber(),
                        etag,
                        command.size(),
                        Instant.now()));
        MultipartUploadPart savedPart = existingPartPresent
                ? multipartUploadPartRepository.update(part)
                : multipartUploadPartRepository.insert(part);
        return MultipartUploadPartAssembler.toDto(savedPart);
    }

    @Transactional
    public StoredObjectDTO completeMultipartUpload(CompleteMultipartUploadCommand command) {
        String uploadId = command.uploadId();
        MultipartUploadSession session = multipartUploadSessionRepository
                .findByUploadId(uploadId)
                .orElseThrow(
                        () -> new NotFoundException("Multipart upload session not found: " + command.uploadId()));
        session.assertOwnership(command.ownerType(), command.ownerId());
        List<MultipartUploadPart> parts = multipartUploadPartRepository.listByUploadId(uploadId);
        session.assertCompletable(parts);
        var storageResult = storedObjectStorageRepository.update(session, parts);
        long storedObjectNoSeed = idGenerator.nextId(STORED_OBJECT_NO_BIZ_TAG);
        StoredObject storedObject = StoredObject.create(
                StoredObjectId.of(idGenerator.nextId(STORED_OBJECT_ID_BIZ_TAG)),
                StoredObjectNo.of(TimestampedBizCodeFormatter.format(STORED_OBJECT_NO_DOMAIN, storedObjectNoSeed)),
                storageResult.storageType(),
                storageResult.bucketName(),
                storageResult.objectKey(),
                session.getOriginalFilename(),
                session.getContentType(),
                session.getTotalSize(),
                storageResult.accessEndpoint());
        StoredObject savedObject = storedObjectRepository.insert(storedObject);
        session.markCompleted();
        multipartUploadSessionRepository.update(session);
        multipartUploadPartRepository.deleteByUploadId(uploadId);
        storageAuditApplicationService.record(
                null,
                savedObject.getId(),
                session.getOwnerType(),
                command.ownerId(),
                StorageAuditActionType.UPLOAD,
                null,
                savedObject.getObjectStatus().value());
        return StoredObjectAssembler.toDto(savedObject);
    }

    @Transactional
    public void abortMultipartUpload(AbortMultipartUploadCommand command) {
        String uploadId = command.uploadId();
        MultipartUploadSession session = multipartUploadSessionRepository
                .findByUploadId(uploadId)
                .orElseThrow(
                        () -> new NotFoundException("Multipart upload session not found: " + command.uploadId()));
        session.assertOwnership(command.ownerType(), command.ownerId());
        session.markAborted();
        multipartUploadSessionRepository.update(session);
        storedObjectStorageRepository.delete(session);
        multipartUploadPartRepository.deleteByUploadId(uploadId);
    }

    @Transactional
    public void markObjectReferenced(StoredObjectReferenceCommand command) {
        StoredObject storedObject = storedObjectRepository
                .findByNo(command.storedObjectNo())
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + command.storedObjectNo()));
        ensureAvailable(storedObject, storedObject.getStoredObjectNo() == null
                ? null
                : storedObject.getStoredObjectNo().value());
        String beforeStatus = storedObject.getReferenceStatus().value();
        StoredObjectReference reference = StoredObjectReference.create(
                storedObject.getId(), command.ownerType(), command.ownerId());
        boolean created = storedObjectReferenceRepository.insert(reference);
        StoredObject savedObject =
                syncReferenceStatus(storedObject, storedObjectReferenceRepository.exists(storedObject.getId()));
        if (!created) {
            return;
        }
        storageAuditApplicationService.record(
                null,
                storedObject.getId(),
                command.ownerType(),
                command.ownerId(),
                StorageAuditActionType.REFERENCE_ADD,
                beforeStatus,
                savedObject.getReferenceStatus().value());
    }

    @Transactional
    public void clearObjectReference(StoredObjectReferenceCommand command) {
        StoredObject storedObject = storedObjectRepository
                .findByNo(command.storedObjectNo())
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + command.storedObjectNo()));
        String beforeStatus = storedObject.getReferenceStatus().value();
        boolean deleted = storedObjectReferenceRepository.delete(
                storedObject.getId(), command.ownerType(), command.ownerId());
        StoredObject savedObject =
                syncReferenceStatus(storedObject, storedObjectReferenceRepository.exists(storedObject.getId()));
        if (!deleted) {
            return;
        }
        storageAuditApplicationService.record(
                null,
                storedObject.getId(),
                command.ownerType(),
                command.ownerId(),
                StorageAuditActionType.REFERENCE_CLEAR,
                beforeStatus,
                savedObject.getReferenceStatus().value());
    }

    public void deleteObject(StoredObjectDeleteCommand command) {
        StoredObject storedObject = storedObjectRepository
                .findByNo(command.storedObjectNo())
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + command.storedObjectNo()));
        StoredObject deletedObject = storedObjectDeletionTransactionService.markDeleting(storedObject.getId());
        if (deletedObject.isDeleted()) {
            return;
        }
        try {
            storedObjectStorageRepository.delete(deletedObject);
            storedObjectDeletionTransactionService.markDeleted(deletedObject.getId());
        } catch (RuntimeException ex) {
            log.warn(
                    "Stored object physical delete failed, objectId={}, objectKey={}, storageType={}",
                    deletedObject.getId(),
                    deletedObject.getObjectKey(),
                    deletedObject.getStorageType(),
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
