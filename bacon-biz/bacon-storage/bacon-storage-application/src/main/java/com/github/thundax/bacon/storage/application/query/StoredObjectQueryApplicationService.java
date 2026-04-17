package com.github.thundax.bacon.storage.application.query;

import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.storage.application.dto.StoredObjectDTO;
import com.github.thundax.bacon.storage.application.dto.StoredObjectPageResultDTO;
import com.github.thundax.bacon.storage.application.assembler.StoredObjectAssembler;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
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

    public StoredObjectDTO getObjectByNo(String storedObjectNo) {
        StoredObjectNo objectNo = StoredObjectNo.of(storedObjectNo);
        StoredObject storedObject = storedObjectRepository
                .findByNo(objectNo)
                .orElseThrow(() -> new NotFoundException("Stored object not found: " + storedObjectNo));
        if (storedObject.isDeleting() || storedObject.isDeleted()) {
            throw new NotFoundException("Stored object is unavailable: " + storedObjectNo);
        }
        return StoredObjectAssembler.toDto(storedObject);
    }

    public StoredObjectPageResultDTO pageObjects(
            StorageType storageType,
            StoredObjectStatus objectStatus,
            StoredObjectReferenceStatus referenceStatus,
            String originalFilename,
            String objectKey,
            Integer pageNo,
            Integer pageSize) {
        int normalizedPageNo = PageParamNormalizer.normalizePageNo(pageNo);
        int normalizedPageSize = PageParamNormalizer.normalizePageSize(pageSize);
        long total = storedObjectRepository.countObjects(
                storageType, objectStatus, referenceStatus, originalFilename, objectKey);
        List<StoredObjectDTO> records = (total <= 0
                        ? List.<StoredObject>of()
                        : storedObjectRepository.pageObjects(
                                storageType,
                                objectStatus,
                                referenceStatus,
                                originalFilename,
                                objectKey,
                                normalizedPageNo,
                                normalizedPageSize))
                .stream().map(StoredObjectAssembler::toDto).toList();
        return new StoredObjectPageResultDTO(records, total, normalizedPageNo, normalizedPageSize);
    }
}
