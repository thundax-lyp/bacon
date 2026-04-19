package com.github.thundax.bacon.storage.application.support;

import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.storage.application.config.StorageUploadLimitProperties;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import org.springframework.stereotype.Component;

/**
 * Storage 上传尺寸限制校验器。
 */
@Component
public class StorageUploadLimitValidator {

    private final StorageUploadLimitProperties properties;

    public StorageUploadLimitValidator(StorageUploadLimitProperties properties) {
        this.properties = properties;
    }

    public void validateSingleUpload(Long size) {
        requirePositive(size, "single upload size");
        if (size > properties.getSingleUploadMaxSizeBytes()) {
            throw new BadRequestException("Single upload size exceeds configured limit");
        }
    }

    public void validateMultipartInit(Long totalSize, Long partSize) {
        requirePositive(totalSize, "multipart totalSize");
        requirePositive(partSize, "multipart partSize");
        if (totalSize > properties.getMultipartMaxTotalSizeBytes()) {
            throw new BadRequestException("Multipart totalSize exceeds configured limit");
        }
        if (partSize != properties.getMultipartPartSizeBytes()) {
            throw new BadRequestException("Multipart partSize must equal configured size");
        }
    }

    public void validateMultipartPartUpload(MultipartUploadSession session, Long size) {
        requirePositive(size, "multipart part size");
        if (session.getPartSize() != properties.getMultipartPartSizeBytes()) {
            throw new BadRequestException("Multipart session partSize does not match configured size");
        }
        if (size > properties.getMultipartPartSizeBytes()) {
            throw new BadRequestException("Multipart part size exceeds configured size");
        }
        if (size > session.getPartSize()) {
            throw new BadRequestException("Multipart part size exceeds session partSize");
        }
    }

    private void requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0L) {
            throw new BadRequestException(fieldName + " must be positive");
        }
    }
}
