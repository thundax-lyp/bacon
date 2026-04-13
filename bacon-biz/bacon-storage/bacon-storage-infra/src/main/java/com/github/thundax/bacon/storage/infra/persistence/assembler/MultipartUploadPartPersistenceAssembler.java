package com.github.thundax.bacon.storage.infra.persistence.assembler;

import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.MultipartUploadPartDO;
import java.util.Objects;

public final class MultipartUploadPartPersistenceAssembler {

    private MultipartUploadPartPersistenceAssembler() {}

    public static MultipartUploadPartDO toDataObject(MultipartUploadPart multipartUploadPart) {
        if (multipartUploadPart == null) {
            return null;
        }
        Objects.requireNonNull(multipartUploadPart.getId(), "multipartUploadPart.id must not be null");
        return new MultipartUploadPartDO(
                multipartUploadPart.getId(),
                multipartUploadPart.getUploadId(),
                multipartUploadPart.getPartNumber(),
                multipartUploadPart.getEtag(),
                multipartUploadPart.getSize(),
                multipartUploadPart.getCreatedAt());
    }

    public static MultipartUploadPart toDomain(MultipartUploadPartDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return MultipartUploadPart.reconstruct(
                dataObject.getId(),
                dataObject.getUploadId(),
                dataObject.getPartNumber(),
                dataObject.getEtag(),
                dataObject.getSize(),
                dataObject.getCreatedAt());
    }
}
