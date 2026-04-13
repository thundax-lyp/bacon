package com.github.thundax.bacon.storage.application.query;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectPageQueryDTO;
import com.github.thundax.bacon.storage.api.dto.StoredObjectPageResultDTO;
import com.github.thundax.bacon.storage.application.assembler.StoredObjectAssembler;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import java.util.List;
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
        StoredObjectId storedObjectId = StoredObjectId.of(objectId);
        StoredObject storedObject = storedObjectRepository
                .findById(storedObjectId)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + objectId));
        if (storedObject.isDeleting() || storedObject.isDeleted()) {
            throw new NotFoundException("Stored object is unavailable: " + objectId);
        }
        return StoredObjectAssembler.toDto(storedObject);
    }

    public StoredObjectPageResultDTO pageObjects(StoredObjectPageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        long total = storedObjectRepository.countObjects(
                query.getStorageType(),
                query.getObjectStatus(),
                query.getReferenceStatus(),
                query.getOriginalFilename(),
                query.getObjectKey());
        List<StoredObjectDTO> records = (total <= 0
                        ? List.<StoredObject>of()
                        : storedObjectRepository.pageObjects(
                                query.getStorageType(),
                                query.getObjectStatus(),
                                query.getReferenceStatus(),
                                query.getOriginalFilename(),
                                query.getObjectKey(),
                                offset,
                                pageSize))
                .stream().map(StoredObjectAssembler::toDto).toList();
        return new StoredObjectPageResultDTO(records, total, pageNo, pageSize);
    }
}
