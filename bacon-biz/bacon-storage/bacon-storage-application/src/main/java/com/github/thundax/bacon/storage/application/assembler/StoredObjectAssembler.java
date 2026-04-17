package com.github.thundax.bacon.storage.application.assembler;

import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;

public final class StoredObjectAssembler {

    private StoredObjectAssembler() {}

    public static StoredObjectDTO toDto(StoredObject storedObject) {
        if (storedObject == null) {
            return null;
        }
        return new StoredObjectDTO(
                storedObject.getId() == null ? null : storedObject.getId().externalValue(),
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

    public static StoredObject toDomain(StoredObjectDTO storedObjectDTO) {
        if (storedObjectDTO == null) {
            return null;
        }
        return StoredObject.reconstruct(
                toStoredObjectId(storedObjectDTO.getId()),
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

    private static StoredObjectId toStoredObjectId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        String normalized = id.startsWith("O") ? id.substring(1) : id;
        return StoredObjectId.of(Long.valueOf(normalized));
    }
}
