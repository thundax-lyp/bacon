package com.github.thundax.bacon.storage.api.facade;

import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;

public interface StoredObjectFacade {

    StoredObjectDTO uploadObject(UploadObjectCommand command);

    StoredObjectDTO getObjectById(Long objectId);

    void markObjectReferenced(Long objectId, String ownerType, String ownerId);

    void clearObjectReference(Long objectId, String ownerType, String ownerId);

    void deleteObject(Long objectId);
}
