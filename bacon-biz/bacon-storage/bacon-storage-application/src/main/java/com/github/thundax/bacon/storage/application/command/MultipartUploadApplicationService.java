package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.storage.api.dto.CompleteMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.InitMultipartUploadCommand;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadMultipartPartCommand;
import com.github.thundax.bacon.storage.api.enums.UploadStatusEnum;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 分段上传应用服务骨架。
 */
@Service
public class MultipartUploadApplicationService {

    public MultipartUploadSessionDTO initMultipartUpload(InitMultipartUploadCommand command) {
        return new MultipartUploadSessionDTO(UUID.randomUUID().toString(), command.getOwnerType(), command.getTenantId(),
                command.getCategory(), command.getOriginalFilename(), command.getContentType(), command.getTotalSize(),
                command.getPartSize(), 0, UploadStatusEnum.INITIATED.name());
    }

    public MultipartUploadPartDTO uploadMultipartPart(UploadMultipartPartCommand command) {
        return new MultipartUploadPartDTO(command.getUploadId(), command.getPartNumber(), "PENDING");
    }

    public StoredObjectDTO completeMultipartUpload(CompleteMultipartUploadCommand command) {
        throw new UnsupportedOperationException("Multipart upload completion not implemented yet");
    }

    public void abortMultipartUpload(String uploadId) {
        throw new UnsupportedOperationException("Multipart upload abort not implemented yet");
    }
}
