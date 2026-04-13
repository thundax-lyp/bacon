package com.github.thundax.bacon.storage.infra.persistence.assembler;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObjectReference;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StoredObjectReferenceDO;

public final class StoredObjectReferencePersistenceAssembler {

    private StoredObjectReferencePersistenceAssembler() {}

    public static StoredObjectReferenceDO toDataObject(StoredObjectReference storedObjectReference) {
        if (storedObjectReference == null) {
            return null;
        }
        return new StoredObjectReferenceDO(
                storedObjectReference.getObjectId() == null
                        ? null
                        : storedObjectReference.getObjectId().value(),
                storedObjectReference.getOwnerType(),
                storedObjectReference.getOwnerId());
    }

    public static StoredObjectReference toDomain(StoredObjectReferenceDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return StoredObjectReference.reconstruct(
                dataObject.getObjectId() == null ? null : StoredObjectId.of(dataObject.getObjectId()),
                dataObject.getOwnerType(),
                dataObject.getOwnerId());
    }
}
