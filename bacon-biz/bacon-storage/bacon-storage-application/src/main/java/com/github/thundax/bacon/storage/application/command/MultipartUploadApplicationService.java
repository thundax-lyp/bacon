package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 分段上传应用服务骨架。
 */
@Service
public class MultipartUploadApplicationService {

    public MultipartUploadSessionDTO initMultipartUpload(InitMultipartUploadCommand command) {
        MultipartUploadSession session = MultipartUploadSession.initiate(UUID.randomUUID().toString(),
                command.getTenantId(), command.getOwnerType(), command.getCategory(), command.getOriginalFilename(),
                command.getContentType(), command.getTotalSize(), command.getPartSize());
        return new MultipartUploadSessionDTO(session.getUploadId(), session.getOwnerType(), session.getTenantId(),
                session.getCategory(), session.getOriginalFilename(), session.getContentType(), session.getTotalSize(),
                session.getPartSize(), session.getUploadedPartCount(), session.getUploadStatus());
    }

    public MultipartUploadPartDTO uploadMultipartPart(UploadMultipartPartCommand command) {
        MultipartUploadPart part = MultipartUploadPart.create(command.getUploadId(), command.getPartNumber(),
                "PART-" + command.getPartNumber(), command.getSize());
        return new MultipartUploadPartDTO(part.getUploadId(), part.getPartNumber(), part.getEtag());
    }

    public StoredObjectDTO completeMultipartUpload(CompleteMultipartUploadCommand command) {
        throw new UnsupportedOperationException("Multipart upload completion not implemented yet");
    }

    public void abortMultipartUpload(String uploadId) {
        throw new UnsupportedOperationException("Multipart upload abort not implemented yet");
    }
}
