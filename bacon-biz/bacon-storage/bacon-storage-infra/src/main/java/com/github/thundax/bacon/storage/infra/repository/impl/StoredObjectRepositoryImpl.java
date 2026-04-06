package com.github.thundax.bacon.storage.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StoredObjectDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.StoredObjectMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public class StoredObjectRepositoryImpl implements StoredObjectRepository {

    private final StoredObjectMapper storedObjectMapper;
    private final Ids ids;

    public StoredObjectRepositoryImpl(StoredObjectMapper storedObjectMapper, Ids ids) {
        this.storedObjectMapper = storedObjectMapper;
        this.ids = ids;
    }

    @Override
    public StoredObject save(StoredObject storedObject) {
        StoredObjectDO dataObject = toDataObject(storedObject);
        if (dataObject.getId() == null) {
            dataObject.setId(ids.storedObjectId());
            storedObjectMapper.insert(dataObject);
        } else {
            storedObjectMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    @Override
    public Optional<StoredObject> findById(StoredObjectId objectId) {
        return Optional.ofNullable(storedObjectMapper.selectById(objectId)).map(this::toDomain);
    }

    @Override
    public List<StoredObject> listByObjectStatus(StoredObjectStatus objectStatus, int limit) {
        return storedObjectMapper.selectList(Wrappers.<StoredObjectDO>lambdaQuery()
                        .eq(StoredObjectDO::getObjectStatus, objectStatus == null ? null : objectStatus.value())
                        .orderByAsc(StoredObjectDO::getUpdatedAt, StoredObjectDO::getId)
                        .last("limit " + limit))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<StoredObject> pageObjects(Long tenantId, String storageType, String objectStatus,
                                          String referenceStatus, String originalFilename, String objectKey,
                                          int offset, int limit) {
        LambdaQueryWrapper<StoredObjectDO> listWrapper = buildQueryWrapper(tenantId, storageType, objectStatus,
                referenceStatus, originalFilename, objectKey)
                .orderByDesc(StoredObjectDO::getCreatedAt, StoredObjectDO::getId)
                .last("LIMIT " + offset + "," + limit);
        return storedObjectMapper.selectList(listWrapper).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countObjects(Long tenantId, String storageType, String objectStatus, String referenceStatus,
                             String originalFilename, String objectKey) {
        LambdaQueryWrapper<StoredObjectDO> countWrapper = buildQueryWrapper(tenantId, storageType, objectStatus,
                referenceStatus, originalFilename, objectKey);
        return storedObjectMapper.selectCount(countWrapper);
    }

    private LambdaQueryWrapper<StoredObjectDO> buildQueryWrapper(Long tenantId, String storageType, String objectStatus,
                                                                 String referenceStatus, String originalFilename,
                                                                 String objectKey) {
        LambdaQueryWrapper<StoredObjectDO> wrapper = Wrappers.lambdaQuery(StoredObjectDO.class);
        if (tenantId != null) {
            wrapper.eq(StoredObjectDO::getTenantId, TenantId.of(tenantId));
        }
        if (storageType != null && !storageType.isBlank()) {
            wrapper.eq(StoredObjectDO::getStorageType, storageType.trim());
        }
        if (objectStatus != null && !objectStatus.isBlank()) {
            wrapper.eq(StoredObjectDO::getObjectStatus, objectStatus.trim());
        }
        if (referenceStatus != null && !referenceStatus.isBlank()) {
            wrapper.eq(StoredObjectDO::getReferenceStatus, referenceStatus.trim());
        }
        if (originalFilename != null && !originalFilename.isBlank()) {
            wrapper.like(StoredObjectDO::getOriginalFilename, originalFilename.trim());
        }
        if (objectKey != null && !objectKey.isBlank()) {
            wrapper.like(StoredObjectDO::getObjectKey, objectKey.trim());
        }
        return wrapper;
    }

    private StoredObjectDO toDataObject(StoredObject storedObject) {
        return new StoredObjectDO(storedObject.getId(), storedObject.getTenantId(),
                storedObject.getStorageType() == null ? null : storedObject.getStorageType().value(),
                storedObject.getBucketName(), storedObject.getObjectKey(), storedObject.getOriginalFilename(),
                storedObject.getContentType(), storedObject.getSize(), storedObject.getAccessEndpoint(),
                storedObject.getObjectStatus() == null ? null : storedObject.getObjectStatus().value(),
                storedObject.getReferenceStatus() == null ? null : storedObject.getReferenceStatus().value(),
                storedObject.getCreatedBy(),
                storedObject.getCreatedAt(), storedObject.getUpdatedBy(), storedObject.getUpdatedAt());
    }

    private StoredObject toDomain(StoredObjectDO dataObject) {
        return new StoredObject(dataObject.getId(), dataObject.getTenantId(), StorageType.fromValue(dataObject.getStorageType()),
                dataObject.getBucketName(), dataObject.getObjectKey(), dataObject.getOriginalFilename(),
                dataObject.getContentType(), dataObject.getSize(), dataObject.getAccessEndpoint(),
                StoredObjectStatus.fromValue(dataObject.getObjectStatus()),
                StoredObjectReferenceStatus.fromValue(dataObject.getReferenceStatus()), dataObject.getCreatedBy(),
                dataObject.getCreatedAt(), dataObject.getUpdatedBy(), dataObject.getUpdatedAt());
    }
}
