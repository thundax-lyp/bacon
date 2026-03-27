package com.github.thundax.bacon.storage.interfaces.facade;

import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.api.facade.StoredObjectFacade;
import com.github.thundax.bacon.storage.application.command.StoredObjectApplicationService;
import com.github.thundax.bacon.storage.application.query.StoredObjectQueryApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class StoredObjectFacadeLocalImpl implements StoredObjectFacade {

    private final StoredObjectApplicationService storedObjectApplicationService;
    private final StoredObjectQueryApplicationService storedObjectQueryApplicationService;

    public StoredObjectFacadeLocalImpl(StoredObjectApplicationService storedObjectApplicationService,
                                       StoredObjectQueryApplicationService storedObjectQueryApplicationService) {
        this.storedObjectApplicationService = storedObjectApplicationService;
        this.storedObjectQueryApplicationService = storedObjectQueryApplicationService;
    }

    @Override
    public StoredObjectDTO uploadObject(UploadObjectCommand command) {
        return storedObjectApplicationService.uploadObject(command);
    }

    @Override
    public StoredObjectDTO getObjectById(Long objectId) {
        return storedObjectQueryApplicationService.getObjectById(objectId);
    }

    @Override
    public void markObjectReferenced(Long objectId, String ownerType, String ownerId) {
        storedObjectApplicationService.markObjectReferenced(objectId, ownerType, ownerId);
    }

    @Override
    public void clearObjectReference(Long objectId, String ownerType, String ownerId) {
        storedObjectApplicationService.clearObjectReference(objectId, ownerType, ownerId);
    }

    @Override
    public void deleteObject(Long objectId) {
        storedObjectApplicationService.deleteObject(objectId);
    }
}
