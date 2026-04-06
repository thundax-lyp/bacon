package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.storage.api.dto.AbortMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.api.enums.UploadStatusEnum;
import com.github.thundax.bacon.storage.application.support.StorageAuditApplicationService;
import com.github.thundax.bacon.storage.application.support.StorageUploadLimitValidator;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageAuditActionType;
import com.github.thundax.bacon.storage.domain.model.enums.UploadStatus;
import com.github.thundax.bacon.storage.domain.model.valueobject.MultipartUploadStorageSession;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadPartRepository;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadSessionRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * 分段上传应用服务骨架。
 */
@Service
public class MultipartUploadApplicationService {

    private static final String UPLOAD_ID_BIZ_TAG = "storage_multipart_upload";

    private final MultipartUploadSessionRepository multipartUploadSessionRepository;
    private final MultipartUploadPartRepository multipartUploadPartRepository;
    private final StoredObjectRepository storedObjectRepository;
    private final StoredObjectStorageRepository storedObjectStorageRepository;
    private final StorageAuditApplicationService storageAuditApplicationService;
    private final StorageUploadLimitValidator storageUploadLimitValidator;
    private final IdGenerator idGenerator;

    public MultipartUploadApplicationService(MultipartUploadSessionRepository multipartUploadSessionRepository,
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
        Long uploadId = idGenerator.nextId(UPLOAD_ID_BIZ_TAG);
        MultipartUploadSession session = MultipartUploadSession.initiate(String.valueOf(uploadId),
                toTenantId(command.getTenantId()), command.getOwnerType(), command.getOwnerId(), command.getCategory(),
                command.getOriginalFilename(), command.getContentType(), storageSession.objectKey(), storageSession.providerUploadId(),
                command.getTotalSize(), command.getPartSize());
        MultipartUploadSession savedSession = multipartUploadSessionRepository.save(session);
        return new MultipartUploadSessionDTO(toUploadId(savedSession.getUploadId()), savedSession.getOwnerType(), savedSession.getOwnerId(),
                savedSession.getTenantId() == null ? null : savedSession.getTenantId().value(),
                savedSession.getCategory(), savedSession.getOriginalFilename(),
                savedSession.getContentType(), savedSession.getTotalSize(), savedSession.getPartSize(),
                savedSession.getUploadedPartCount(), toUploadStatusEnum(savedSession.getUploadStatus()));
    }

    @Transactional
    public MultipartUploadPartDTO uploadMultipartPart(UploadMultipartPartCommand command) {
        String uploadId = toUploadIdValue(command.getUploadId());
        MultipartUploadSession session = multipartUploadSessionRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new NotFoundException("Multipart upload session not found: " + command.getUploadId()));
        session.assertOwnership(toTenantId(command.getTenantId()), command.getOwnerType(), command.getOwnerId());
        storageUploadLimitValidator.validateMultipartPartUpload(session, command.getSize());
        boolean existingPartPresent = multipartUploadPartRepository.findByUploadIdAndPartNumber(uploadId,
                command.getPartNumber()).isPresent();
        String etag = storedObjectStorageRepository.uploadPart(session, command.getPartNumber(), command.getSize(),
                command.getInputStream());
        if (!existingPartPresent) {
            session.recordUploadedPart();
            multipartUploadSessionRepository.save(session);
        }
        MultipartUploadPart part = multipartUploadPartRepository.findByUploadIdAndPartNumber(uploadId,
                        command.getPartNumber())
                .map(existingPart -> new MultipartUploadPart(existingPart.getId(), existingPart.getUploadId(),
                        existingPart.getPartNumber(), etag, command.getSize(), existingPart.getCreatedAt()))
                .orElseGet(() -> MultipartUploadPart.create(uploadId, command.getPartNumber(), etag,
                        command.getSize()));
        MultipartUploadPart savedPart = multipartUploadPartRepository.save(part);
        return new MultipartUploadPartDTO(savedPart.getUploadId(), savedPart.getPartNumber(), savedPart.getEtag());
    }

    @Transactional
    public StoredObjectDTO completeMultipartUpload(CompleteMultipartUploadCommand command) {
        String uploadId = toUploadIdValue(command.getUploadId());
        MultipartUploadSession session = multipartUploadSessionRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new NotFoundException("Multipart upload session not found: " + command.getUploadId()));
        session.assertOwnership(toTenantId(command.getTenantId()), command.getOwnerType(), command.getOwnerId());
        List<MultipartUploadPart> parts = multipartUploadPartRepository.listByUploadId(uploadId);
        session.assertCompletable(parts);
        var storageResult = storedObjectStorageRepository.completeMultipartUpload(session, parts);
        StoredObject storedObject = StoredObject.newUploadedObject(session.getTenantId(), storageResult.getStorageType(),
                storageResult.getBucketName(), storageResult.getObjectKey(), session.getOriginalFilename(),
                session.getContentType(), session.getTotalSize(), storageResult.getAccessEndpoint(), null);
        StoredObject savedObject = storedObjectRepository.save(storedObject);
        session.markCompleted();
        multipartUploadSessionRepository.save(session);
        multipartUploadPartRepository.deleteByUploadId(uploadId);
        storageAuditApplicationService.record(savedObject.getTenantId(), savedObject.getId(), session.getOwnerType(),
                command.getOwnerId(), StorageAuditActionType.UPLOAD, null, savedObject.getObjectStatus().value());
        return new StoredObjectDTO(savedObject.getId(),
                savedObject.getStorageType() == null ? null : savedObject.getStorageType().value(), savedObject.getBucketName(),
                savedObject.getObjectKey(), savedObject.getOriginalFilename(), savedObject.getContentType(),
                savedObject.getSize(), savedObject.getAccessEndpoint(), savedObject.getObjectStatus().value(),
                savedObject.getReferenceStatus().value(), savedObject.getCreatedAt());
    }

    private TenantId toTenantId(Long tenantId) {
        return tenantId == null ? null : TenantId.of(tenantId);
    }

    private UploadStatusEnum toUploadStatusEnum(UploadStatus uploadStatus) {
        return uploadStatus == null ? null : UploadStatusEnum.valueOf(uploadStatus.name());
    }

    @Transactional
    public void abortMultipartUpload(AbortMultipartUploadCommand command) {
        String uploadId = toUploadIdValue(command.getUploadId());
        MultipartUploadSession session = multipartUploadSessionRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new NotFoundException("Multipart upload session not found: " + command.getUploadId()));
        session.assertOwnership(toTenantId(command.getTenantId()), command.getOwnerType(), command.getOwnerId());
        session.markAborted();
        multipartUploadSessionRepository.save(session);
        storedObjectStorageRepository.abortMultipartUpload(session);
        multipartUploadPartRepository.deleteByUploadId(uploadId);
    }

    private Long toUploadId(String uploadId) {
        return uploadId == null ? null : Long.valueOf(uploadId);
    }

    private String toUploadIdValue(Long uploadId) {
        return uploadId == null ? null : String.valueOf(uploadId);
    }
}
