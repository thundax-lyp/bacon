package com.github.thundax.bacon.storage.infra.persistence.assembler;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StoredObjectDO;
import java.util.Objects;

public final class StoredObjectPersistenceAssembler {

    private StoredObjectPersistenceAssembler() {}

    public static StoredObjectDO toDataObject(StoredObject storedObject) {
        if (storedObject == null) {
            return null;
        }
        Objects.requireNonNull(storedObject.getId(), "storedObject.id must not be null");
        return new StoredObjectDO(
                storedObject.getId() == null ? null : storedObject.getId().value(),
                BaconContextHolder.requireTenantId(),
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
                        : storedObject.getReferenceStatus().value());
    }

    public static StoredObject toDomain(StoredObjectDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return StoredObject.reconstruct(
                dataObject.getId() == null ? null : StoredObjectId.of(dataObject.getId()),
                dataObject.getStorageType() == null ? null : StorageType.from(dataObject.getStorageType()),
                dataObject.getBucketName(),
                dataObject.getObjectKey(),
                dataObject.getOriginalFilename(),
                dataObject.getContentType(),
                dataObject.getSize(),
                dataObject.getAccessEndpoint(),
                dataObject.getObjectStatus() == null ? null : StoredObjectStatus.from(dataObject.getObjectStatus()),
                dataObject.getReferenceStatus() == null
                        ? null
                        : StoredObjectReferenceStatus.from(dataObject.getReferenceStatus()));
    }
}
