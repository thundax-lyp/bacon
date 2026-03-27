package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadPartRepository;
import com.github.thundax.bacon.storage.domain.repository.MultipartUploadSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 分段上传应用服务骨架。
 */
@Service
public class MultipartUploadApplicationService {

    private final MultipartUploadSessionRepository multipartUploadSessionRepository;
    private final MultipartUploadPartRepository multipartUploadPartRepository;

    public MultipartUploadApplicationService(MultipartUploadSessionRepository multipartUploadSessionRepository,
                                            MultipartUploadPartRepository multipartUploadPartRepository) {
        this.multipartUploadSessionRepository = multipartUploadSessionRepository;
        this.multipartUploadPartRepository = multipartUploadPartRepository;
    }

    @Transactional
    public MultipartUploadSessionDTO initMultipartUpload(InitMultipartUploadCommand command) {
        MultipartUploadSession session = MultipartUploadSession.initiate(UUID.randomUUID().toString(),
                command.getTenantId(), command.getOwnerType(), command.getCategory(), command.getOriginalFilename(),
                command.getContentType(), command.getTotalSize(), command.getPartSize());
        MultipartUploadSession savedSession = multipartUploadSessionRepository.save(session);
        return new MultipartUploadSessionDTO(savedSession.getUploadId(), savedSession.getOwnerType(),
                savedSession.getTenantId(), savedSession.getCategory(), savedSession.getOriginalFilename(),
                savedSession.getContentType(), savedSession.getTotalSize(), savedSession.getPartSize(),
                savedSession.getUploadedPartCount(), savedSession.getUploadStatus());
    }

    @Transactional
    public MultipartUploadPartDTO uploadMultipartPart(UploadMultipartPartCommand command) {
        MultipartUploadSession session = multipartUploadSessionRepository.findByUploadId(command.getUploadId())
                .orElseThrow(() -> new NotFoundException("Multipart upload session not found: " + command.getUploadId()));
        session.recordUploadedPart();
        multipartUploadSessionRepository.save(session);
        MultipartUploadPart part = MultipartUploadPart.create(command.getUploadId(), command.getPartNumber(),
                "PART-" + command.getPartNumber(), command.getSize());
        MultipartUploadPart savedPart = multipartUploadPartRepository.save(part);
        return new MultipartUploadPartDTO(savedPart.getUploadId(), savedPart.getPartNumber(), savedPart.getEtag());
    }

    public StoredObjectDTO completeMultipartUpload(CompleteMultipartUploadCommand command) {
        throw new UnsupportedOperationException("Multipart upload completion not implemented yet");
    }

    @Transactional
    public void abortMultipartUpload(String uploadId) {
        MultipartUploadSession session = multipartUploadSessionRepository.findByUploadId(uploadId)
                .orElseThrow(() -> new NotFoundException("Multipart upload session not found: " + uploadId));
        session.markAborted();
        multipartUploadSessionRepository.save(session);
        multipartUploadPartRepository.deleteByUploadId(uploadId);
    }
}
