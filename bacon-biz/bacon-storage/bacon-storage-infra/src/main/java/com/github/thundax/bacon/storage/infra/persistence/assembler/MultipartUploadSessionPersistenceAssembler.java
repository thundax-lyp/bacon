package com.github.thundax.bacon.storage.infra.persistence.assembler;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.MultipartUploadSessionDO;
import java.util.Objects;

public final class MultipartUploadSessionPersistenceAssembler {

    private MultipartUploadSessionPersistenceAssembler() {}

    public static MultipartUploadSessionDO toDataObject(MultipartUploadSession multipartUploadSession) {
        if (multipartUploadSession == null) {
            return null;
        }
        Objects.requireNonNull(multipartUploadSession.getId(), "multipartUploadSession.id must not be null");
        return new MultipartUploadSessionDO(
                multipartUploadSession.getId(),
                multipartUploadSession.getUploadId(),
                BaconContextHolder.requireTenantId(),
                multipartUploadSession.getOwnerType(),
                multipartUploadSession.getOwnerId(),
                multipartUploadSession.getCategory(),
                multipartUploadSession.getOriginalFilename(),
                multipartUploadSession.getContentType(),
                multipartUploadSession.getObjectKey(),
                multipartUploadSession.getProviderUploadId(),
                multipartUploadSession.getTotalSize(),
                multipartUploadSession.getPartSize(),
                multipartUploadSession.getUploadedPartCount(),
                multipartUploadSession.getUploadStatus(),
                multipartUploadSession.getCreatedAt(),
                multipartUploadSession.getUpdatedAt(),
                multipartUploadSession.getCompletedAt(),
                multipartUploadSession.getAbortedAt());
    }

    public static MultipartUploadSession toDomain(MultipartUploadSessionDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return MultipartUploadSession.reconstruct(
                dataObject.getId(),
                dataObject.getUploadId(),
                dataObject.getOwnerType(),
                dataObject.getOwnerId(),
                dataObject.getCategory(),
                dataObject.getOriginalFilename(),
                dataObject.getContentType(),
                dataObject.getObjectKey(),
                dataObject.getProviderUploadId(),
                dataObject.getTotalSize(),
                dataObject.getPartSize(),
                dataObject.getUploadedPartCount(),
                dataObject.getUploadStatus(),
                dataObject.getCreatedAt(),
                dataObject.getUpdatedAt(),
                dataObject.getCompletedAt(),
                dataObject.getAbortedAt());
    }
}
