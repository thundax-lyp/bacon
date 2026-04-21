package com.github.thundax.bacon.storage.application.query;

import com.github.thundax.bacon.common.application.page.PageResult;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.storage.application.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.application.assembler.StoredObjectAssembler;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectNo;
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

    public StoredObjectDTO getObjectByNo(StoredObjectGetQuery query) {
        StoredObjectNo objectNo = query == null ? null : query.storedObjectNo();
        String storedObjectNo = objectNo == null ? null : objectNo.value();
        StoredObject storedObject = storedObjectRepository
                .findByNo(objectNo)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + storedObjectNo));
        if (storedObject.isDeleting() || storedObject.isDeleted()) {
            throw new NotFoundException("Stored object is unavailable: " + storedObjectNo);
        }
        return StoredObjectAssembler.toDto(storedObject);
    }

    public PageResult<StoredObjectDTO> page(StoredObjectPageQuery query) {
        StoredObjectPageQuery pageQuery = query == null ? new StoredObjectPageQuery(null, null, null, null, null, 1, 20) : query;
        long total = storedObjectRepository.count(
                pageQuery.getStorageType(),
                pageQuery.getObjectStatus(),
                pageQuery.getReferenceStatus(),
                pageQuery.getOriginalFilename(),
                pageQuery.getObjectKey());
        List<StoredObjectDTO> records = (total <= 0
                        ? List.<StoredObject>of()
                        : storedObjectRepository.page(
                                pageQuery.getStorageType(),
                                pageQuery.getObjectStatus(),
                                pageQuery.getReferenceStatus(),
                                pageQuery.getOriginalFilename(),
                                pageQuery.getObjectKey(),
                                pageQuery.getPageNo(),
                                pageQuery.getPageSize()))
                .stream().map(StoredObjectAssembler::toDto).toList();
        return new PageResult<>(records, total, pageQuery.getPageNo(), pageQuery.getPageSize());
    }
}
