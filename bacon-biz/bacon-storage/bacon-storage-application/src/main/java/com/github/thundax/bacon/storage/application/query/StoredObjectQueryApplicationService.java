package com.github.thundax.bacon.storage.application.query;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.api.dto.StoredObjectPageQueryDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectPageResultDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 存储对象查询应用服务。
 */
@Service
public class StoredObjectQueryApplicationService {

    private final StoredObjectRepository storedObjectRepository;

    public StoredObjectQueryApplicationService(StoredObjectRepository storedObjectRepository) {
        this.storedObjectRepository = storedObjectRepository;
    }

    public StoredObjectDTO getObjectById(String objectId) {
        StoredObjectId storedObjectId = StoredObjectId.of(toObjectId(objectId));
        StoredObject storedObject = storedObjectRepository.findById(storedObjectId)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + objectId));
        if (storedObject.isDeleting() || storedObject.isDeleted()) {
            throw new NotFoundException("Stored object is unavailable: " + objectId);
        }
        return toDto(storedObject);
    }

    public StoredObjectPageResultDTO pageObjects(StoredObjectPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        long total = storedObjectRepository.countObjects(query.getTenantId(), query.getStorageType(),
                query.getObjectStatus(), query.getReferenceStatus(), query.getOriginalFilename(), query.getObjectKey());
        List<StoredObjectDTO> records = (total <= 0 ? List.<StoredObject>of()
                : storedObjectRepository.pageObjects(query.getTenantId(), query.getStorageType(), query.getObjectStatus(),
                query.getReferenceStatus(), query.getOriginalFilename(), query.getObjectKey(), offset, pageSize)).stream()
                .map(this::toDto)
                .toList();
        return new StoredObjectPageResultDTO(records, total, pageNo, pageSize);
    }

    private StoredObjectDTO toDto(StoredObject storedObject) {
        return new StoredObjectDTO(storedObject.getId(),
                storedObject.getStorageType() == null ? null : storedObject.getStorageType().value(),
                storedObject.getBucketName(),
                storedObject.getObjectKey(), storedObject.getOriginalFilename(), storedObject.getContentType(),
                storedObject.getSize(), storedObject.getAccessEndpoint(), storedObject.getObjectStatus().value(),
                storedObject.getReferenceStatus().value(), storedObject.getCreatedAt());
    }

    private Long toObjectId(String objectId) {
        String normalized = objectId.startsWith("O") ? objectId.substring(1) : objectId;
        return Long.valueOf(normalized);
    }
}
