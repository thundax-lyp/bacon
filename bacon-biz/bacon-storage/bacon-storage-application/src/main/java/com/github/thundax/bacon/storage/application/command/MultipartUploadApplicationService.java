package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.TimestampedBizCodeFormatter;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.api.dto.AbortMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.application.assembler.MultipartUploadPartAssembler;
import com.github.thundax.bacon.storage.application.assembler.MultipartUploadSessionAssembler;
import com.github.thundax.bacon.storage.application.assembler.StoredObjectAssembler;
import com.github.thundax.bacon.storage.application.support.StorageAuditApplicationService;
import com.github.thundax.bacon.storage.application.support.StorageUploadLimitValidator;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditActionType;
import com.github.thundax.bacon.storage.domain.model.valueobject.MultipartUploadStorageSession;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectNo;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadPartRepository;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadSessionRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 分段上传应用服务骨架。
 */
@Service
public class MultipartUploadApplicationService {

    private static final String UPLOAD_ID_DOMAIN = "storage";
    private static final String MULTIPART_UPLOAD_BIZ_TAG = "storage_multipart_upload";
    private static final String MULTIPART_UPLOAD_PART_BIZ_TAG = "storage_multipart_upload_part";
    private static final String STORED_OBJECT_ID_BIZ_TAG = "stored-object-id";
    private static final String STORED_OBJECT_NO_BIZ_TAG = "stored-object-no";
    private static final String STORED_OBJECT_NO_DOMAIN = "storage-";

    private final MultipartUploadSessionRepository multipartUploadSessionRepository;
    private final MultipartUploadPartRepository multipartUploadPartRepository;
    private final StoredObjectRepository storedObjectRepository;
    private final StoredObjectStorageRepository storedObjectStorageRepository;
    private final StorageAuditApplicationService storageAuditApplicationService;
    private final StorageUploadLimitValidator storageUploadLimitValidator;
    private final IdGenerator idGenerator;

    public MultipartUploadApplicationService(
            MultipartUploadSessionRepository multipartUploadSessionRepository,
            MultipartUploadPartRepository multipartUploadPartRepository,
            StoredObjectRepository storedObjectRepository,
            StoredObjectStorageRepository storedObjectStorageRepository,
            StorageAuditApplicationService storageAuditApplicationService,
            StorageUploadLimitValidator storageUploadLimitValidator,
            IdGenerator idGenerator) {
        this.multipartUploadSessionRepository = multipartUploadSessionRepository;
        this.multipartUploadPartRepository = multipartUploadPartRepository;
        this.storedObjectRepository = storedObjectRepository;
        this.storedObjectStorageRepository = storedObjectStorageRepository;
        this.storageAuditApplicationService = storageAuditApplicationService;
        this.storageUploadLimitValidator = storageUploadLimitValidator;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public MultipartUploadSessionDTO initMultipartUpload(InitMultipartUploadCommand command) {
        storageUploadLimitValidator.validateMultipartInit(command.getTotalSize(), command.getPartSize());
        MultipartUploadStorageSession storageSession = storedObjectStorageRepository.initMultipartUpload(
                command.getCategory(), command.getOriginalFilename(), command.getContentType());
        Long sessionId = idGenerator.nextId(MULTIPART_UPLOAD_BIZ_TAG);
        String uploadId = TimestampedBizCodeFormatter.format(UPLOAD_ID_DOMAIN, sessionId);
        MultipartUploadSession session = MultipartUploadSession.create(
                sessionId,
                uploadId,
                command.getOwnerType(),
                command.getOwnerId(),
                command.getCategory(),
                command.getOriginalFilename(),
                command.getContentType(),
                storageSession.objectKey(),
                storageSession.providerUploadId(),
                command.getTotalSize(),
                command.getPartSize(),
                Instant.now());
        MultipartUploadSession savedSession = multipartUploadSessionRepository.insert(session);
        return MultipartUploadSessionAssembler.toDto(savedSession);
    }

    @Transactional
    public MultipartUploadPartDTO uploadMultipartPart(UploadMultipartPartCommand command) {
        String uploadId = command.getUploadId();
        MultipartUploadSession session = multipartUploadSessionRepository
                .findByUploadId(uploadId)
                .orElseThrow(
                        () -> new NotFoundException("Multipart upload session not found: " + command.getUploadId()));
        session.assertOwnership(command.getOwnerType(), command.getOwnerId());
        storageUploadLimitValidator.validateMultipartPartUpload(session, command.getSize());
        boolean existingPartPresent = multipartUploadPartRepository
                .findByUploadIdAndPartNumber(uploadId, command.getPartNumber())
                .isPresent();
        String etag = storedObjectStorageRepository.uploadPart(
                session, command.getPartNumber(), command.getSize(), command.getInputStream());
        if (!existingPartPresent) {
            session.recordUploadedPart();
            multipartUploadSessionRepository.update(session);
        }
        MultipartUploadPart part = multipartUploadPartRepository
                .findByUploadIdAndPartNumber(uploadId, command.getPartNumber())
                .map(existingPart -> MultipartUploadPart.reconstruct(
                        existingPart.getId(),
                        existingPart.getUploadId(),
                        existingPart.getPartNumber(),
                        etag,
                        command.getSize(),
                        existingPart.getCreatedAt()))
                .orElseGet(() -> MultipartUploadPart.create(
                        idGenerator.nextId(MULTIPART_UPLOAD_PART_BIZ_TAG),
                        uploadId,
                        command.getPartNumber(),
                        etag,
                        command.getSize(),
                        Instant.now()));
        MultipartUploadPart savedPart = existingPartPresent
                ? multipartUploadPartRepository.update(part)
                : multipartUploadPartRepository.insert(part);
        return MultipartUploadPartAssembler.toDto(savedPart);
    }

    @Transactional
    public StoredObjectDTO completeMultipartUpload(CompleteMultipartUploadCommand command) {
        String uploadId = command.getUploadId();
        MultipartUploadSession session = multipartUploadSessionRepository
                .findByUploadId(uploadId)
                .orElseThrow(
                        () -> new NotFoundException("Multipart upload session not found: " + command.getUploadId()));
        session.assertOwnership(command.getOwnerType(), command.getOwnerId());
        List<MultipartUploadPart> parts = multipartUploadPartRepository.listByUploadId(uploadId);
        session.assertCompletable(parts);
        var storageResult = storedObjectStorageRepository.completeMultipartUpload(session, parts);
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
                command.getOwnerId(),
                StorageAuditActionType.UPLOAD,
                null,
                savedObject.getObjectStatus().value());
        return StoredObjectAssembler.toDto(savedObject);
    }

    @Transactional
    public void abortMultipartUpload(AbortMultipartUploadCommand command) {
        String uploadId = command.getUploadId();
        MultipartUploadSession session = multipartUploadSessionRepository
                .findByUploadId(uploadId)
                .orElseThrow(
                        () -> new NotFoundException("Multipart upload session not found: " + command.getUploadId()));
        session.assertOwnership(command.getOwnerType(), command.getOwnerId());
        session.markAborted();
        multipartUploadSessionRepository.update(session);
        storedObjectStorageRepository.abortMultipartUpload(session);
        multipartUploadPartRepository.deleteByUploadId(uploadId);
    }
}
