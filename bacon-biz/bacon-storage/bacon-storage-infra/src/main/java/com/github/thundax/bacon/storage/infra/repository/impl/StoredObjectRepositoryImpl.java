package com.github.thundax.bacon.storage.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectRepository;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StoredObjectDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.StoredObjectMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class StoredObjectRepositoryImpl implements StoredObjectRepository {

    private static final String BIZ_TAG = "storage_object";

    private final StoredObjectMapper storedObjectMapper;
    private final IdGenerator idGenerator;

    public StoredObjectRepositoryImpl(StoredObjectMapper storedObjectMapper, IdGenerator idGenerator) {
        this.storedObjectMapper = storedObjectMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public StoredObject save(StoredObject storedObject) {
        StoredObjectDO dataObject = toDataObject(storedObject);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId(BIZ_TAG));
            storedObjectMapper.insert(dataObject);
        } else {
            storedObjectMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    @Override
    public Optional<StoredObject> findById(Long objectId) {
        return Optional.ofNullable(storedObjectMapper.selectById(objectId)).map(this::toDomain);
    }

    @Override
    public Optional<StoredObject> findByObjectKey(String objectKey) {
        return Optional.ofNullable(storedObjectMapper.selectOne(Wrappers.<StoredObjectDO>lambdaQuery()
                .eq(StoredObjectDO::getObjectKey, objectKey))).map(this::toDomain);
    }

    @Override
    public void deleteById(Long objectId) {
        storedObjectMapper.deleteById(objectId);
    }

    private StoredObjectDO toDataObject(StoredObject storedObject) {
        return new StoredObjectDO(storedObject.getId(), storedObject.getTenantId(), storedObject.getStorageType(),
                storedObject.getBucketName(), storedObject.getObjectKey(), storedObject.getOriginalFilename(),
                storedObject.getContentType(), storedObject.getSize(), storedObject.getAccessUrl(),
                storedObject.getObjectStatus(), storedObject.getReferenceStatus(), storedObject.getCreatedBy(),
                storedObject.getCreatedAt());
    }

    private StoredObject toDomain(StoredObjectDO dataObject) {
        return new StoredObject(dataObject.getId(), dataObject.getTenantId(), dataObject.getStorageType(),
                dataObject.getBucketName(), dataObject.getObjectKey(), dataObject.getOriginalFilename(),
                dataObject.getContentType(), dataObject.getSize(), dataObject.getAccessUrl(),
                dataObject.getObjectStatus(), dataObject.getReferenceStatus(), dataObject.getCreatedBy(),
                dataObject.getCreatedAt());
    }
}
