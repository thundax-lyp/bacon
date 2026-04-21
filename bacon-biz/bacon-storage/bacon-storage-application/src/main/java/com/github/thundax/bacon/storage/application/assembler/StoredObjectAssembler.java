package com.github.thundax.bacon.storage.application.assembler;

import com.github.thundax.bacon.storage.application.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.application.dto.StoredObjectPageResultDTO;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectNo;
import java.util.List;

public final class StoredObjectAssembler {

    private StoredObjectAssembler() {}

    public static StoredObjectDTO toDto(StoredObject storedObject) {
        if (storedObject == null) {
            return null;
        }
        return new StoredObjectDTO(
                storedObject.getStoredObjectNo() == null ? null : storedObject.getStoredObjectNo().value(),
                storedObject.getStorageType() == null
                        ? null
                        : storedObject.getStorageType().value(),
                storedObject.getBucketName(),
                storedObject.getObjectKey(),
                storedObject.getOriginalFilename(),
                storedObject.getContentType(),
                storedObject.getSize(),
                storedObject.getAccessEndpoint(),
                storedObject.getObjectStatus() == null
                        ? null
                        : storedObject.getObjectStatus().value(),
                storedObject.getReferenceStatus() == null
                        ? null
                        : storedObject.getReferenceStatus().value(),
                null);
    }

    public static StoredObjectPageResultDTO toPageResult(
            List<StoredObjectDTO> records, long total, int pageNo, int pageSize) {
        return new StoredObjectPageResultDTO(records, total, pageNo, pageSize);
    }

    public static StoredObject toDomain(StoredObjectDTO storedObjectDTO) {
        if (storedObjectDTO == null) {
            return null;
        }
        return StoredObject.reconstruct(
                null,
                storedObjectDTO.getStoredObjectNo() == null ? null : StoredObjectNo.of(storedObjectDTO.getStoredObjectNo()),
                storedObjectDTO.getStorageType() == null ? null : StorageType.from(storedObjectDTO.getStorageType()),
                storedObjectDTO.getBucketName(),
                storedObjectDTO.getObjectKey(),
                storedObjectDTO.getOriginalFilename(),
                storedObjectDTO.getContentType(),
                storedObjectDTO.getSize(),
                storedObjectDTO.getAccessEndpoint(),
                storedObjectDTO.getObjectStatus() == null
                        ? null
                        : StoredObjectStatus.from(storedObjectDTO.getObjectStatus()),
                storedObjectDTO.getReferenceStatus() == null
                        ? null
                        : StoredObjectReferenceStatus.from(storedObjectDTO.getReferenceStatus()));
    }

}
