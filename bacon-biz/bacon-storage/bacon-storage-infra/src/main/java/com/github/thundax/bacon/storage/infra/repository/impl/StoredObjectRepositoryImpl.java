package com.github.thundax.bacon.storage.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.infra.persistence.assembler.StoredObjectPersistenceAssembler;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StoredObjectDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.StoredObjectMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class StoredObjectRepositoryImpl implements StoredObjectRepository {

    private final StoredObjectMapper storedObjectMapper;

    public StoredObjectRepositoryImpl(StoredObjectMapper storedObjectMapper) {
        this.storedObjectMapper = storedObjectMapper;
    }

    @Override
    public StoredObject insert(StoredObject storedObject) {
        StoredObjectDO dataObject = StoredObjectPersistenceAssembler.toDataObject(storedObject);
        storedObjectMapper.insert(dataObject);
        return StoredObjectPersistenceAssembler.toDomain(dataObject);
    }

    @Override
    public StoredObject update(StoredObject storedObject) {
        StoredObjectDO dataObject = StoredObjectPersistenceAssembler.toDataObject(storedObject);
        storedObjectMapper.updateById(dataObject);
        return StoredObjectPersistenceAssembler.toDomain(dataObject);
    }

    @Override
    public Optional<StoredObject> findById(StoredObjectId objectId) {
        return Optional.ofNullable(storedObjectMapper.selectById(objectId == null ? null : objectId.value()))
                .map(StoredObjectPersistenceAssembler::toDomain);
    }

    @Override
    public List<StoredObject> listByObjectStatus(StoredObjectStatus objectStatus, int limit) {
        return storedObjectMapper
                .selectList(Wrappers.<StoredObjectDO>lambdaQuery()
                        .eq(StoredObjectDO::getObjectStatus, objectStatus == null ? null : objectStatus.value())
                        .orderByAsc(StoredObjectDO::getId)
                        .last("limit " + limit))
                .stream()
                .map(StoredObjectPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<StoredObject> pageObjects(
            StorageType storageType,
            StoredObjectStatus objectStatus,
            StoredObjectReferenceStatus referenceStatus,
            String originalFilename,
            String objectKey,
            int pageNo,
            int pageSize) {
        int normalizedPageNo = Math.max(pageNo, 1);
        int normalizedPageSize = Math.max(pageSize, 1);
        int offset = Math.max(0, (normalizedPageNo - 1) * normalizedPageSize);
        LambdaQueryWrapper<StoredObjectDO> listWrapper = buildQueryWrapper(
                        storageType, objectStatus, referenceStatus, originalFilename, objectKey)
                .orderByDesc(StoredObjectDO::getId)
                .last("LIMIT " + offset + "," + normalizedPageSize);
        return storedObjectMapper.selectList(listWrapper).stream()
                .map(StoredObjectPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public long countObjects(
            StorageType storageType,
            StoredObjectStatus objectStatus,
            StoredObjectReferenceStatus referenceStatus,
            String originalFilename,
            String objectKey) {
        LambdaQueryWrapper<StoredObjectDO> countWrapper =
                buildQueryWrapper(storageType, objectStatus, referenceStatus, originalFilename, objectKey);
        return storedObjectMapper.selectCount(countWrapper);
    }

    private LambdaQueryWrapper<StoredObjectDO> buildQueryWrapper(
            StorageType storageType,
            StoredObjectStatus objectStatus,
            StoredObjectReferenceStatus referenceStatus,
            String originalFilename,
            String objectKey) {
        LambdaQueryWrapper<StoredObjectDO> wrapper = Wrappers.lambdaQuery(StoredObjectDO.class);
        if (storageType != null) {
            wrapper.eq(StoredObjectDO::getStorageType, storageType.value());
        }
        if (objectStatus != null) {
            wrapper.eq(StoredObjectDO::getObjectStatus, objectStatus.value());
        }
        if (referenceStatus != null) {
            wrapper.eq(StoredObjectDO::getReferenceStatus, referenceStatus.value());
        }
        if (originalFilename != null && !originalFilename.isBlank()) {
            wrapper.like(StoredObjectDO::getOriginalFilename, originalFilename.trim());
        }
        if (objectKey != null && !objectKey.isBlank()) {
            wrapper.like(StoredObjectDO::getObjectKey, objectKey.trim());
        }
        return wrapper;
    }
}
