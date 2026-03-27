package com.github.thundax.bacon.storage.application.query;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import org.springframework.stereotype.Service;

/**
 * 存储对象查询应用服务。
 */
@Service
public class StoredObjectQueryApplicationService {

    private final StoredObjectRepository storedObjectRepository;

    public StoredObjectQueryApplicationService(StoredObjectRepository storedObjectRepository) {
        this.storedObjectRepository = storedObjectRepository;
    }

    public StoredObjectDTO getObjectById(Long objectId) {
        StoredObject storedObject = storedObjectRepository.findById(objectId)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + objectId));
        return new StoredObjectDTO(storedObject.getId(), storedObject.getStorageType(), storedObject.getBucketName(),
                storedObject.getObjectKey(), storedObject.getOriginalFilename(), storedObject.getContentType(),
                storedObject.getSize(), storedObject.getAccessEndpoint(), storedObject.getObjectStatus(),
                storedObject.getReferenceStatus(), storedObject.getCreatedAt());
    }
}
