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
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectPageQuery;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectPageResult;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StoredObjectDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.StoredObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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
    public StoredObjectPageResult pageObjects(StoredObjectPageQuery query) {
        LambdaQueryWrapper<StoredObjectDO> countWrapper = buildQueryWrapper(query);
        long total = storedObjectMapper.selectCount(countWrapper);
        if (total <= 0) {
            return new StoredObjectPageResult(List.of(), 0L);
        }
        LambdaQueryWrapper<StoredObjectDO> listWrapper = buildQueryWrapper(query)
                .orderByDesc(StoredObjectDO::getCreatedAt, StoredObjectDO::getId)
                .last("LIMIT " + query.offset() + "," + query.limit());
        List<StoredObject> records = storedObjectMapper.selectList(listWrapper).stream()
                .map(this::toDomain)
                .toList();
        return new StoredObjectPageResult(records, total);
    }

    private LambdaQueryWrapper<StoredObjectDO> buildQueryWrapper(StoredObjectPageQuery query) {
        LambdaQueryWrapper<StoredObjectDO> wrapper = Wrappers.lambdaQuery(StoredObjectDO.class);
        if (StringUtils.hasText(query.tenantId())) {
            wrapper.eq(StoredObjectDO::getTenantId, TenantId.of(query.tenantId().trim()));
        }
        if (StringUtils.hasText(query.storageType())) {
            wrapper.eq(StoredObjectDO::getStorageType, query.storageType().trim());
        }
        if (StringUtils.hasText(query.objectStatus())) {
            wrapper.eq(StoredObjectDO::getObjectStatus, query.objectStatus().trim());
        }
        if (StringUtils.hasText(query.referenceStatus())) {
            wrapper.eq(StoredObjectDO::getReferenceStatus, query.referenceStatus().trim());
        }
        if (StringUtils.hasText(query.originalFilename())) {
            wrapper.like(StoredObjectDO::getOriginalFilename, query.originalFilename().trim());
        }
        if (StringUtils.hasText(query.objectKey())) {
            wrapper.like(StoredObjectDO::getObjectKey, query.objectKey().trim());
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
