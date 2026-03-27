package com.github.thundax.bacon.storage.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObjectReference;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectReferenceRepository;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StoredObjectReferenceDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.StoredObjectReferenceMapper;
import org.springframework.stereotype.Repository;

@Repository
public class StoredObjectReferenceRepositoryImpl implements StoredObjectReferenceRepository {

    private static final String BIZ_TAG = "storage_object_reference";

    private final StoredObjectReferenceMapper storedObjectReferenceMapper;
    private final IdGenerator idGenerator;

    public StoredObjectReferenceRepositoryImpl(StoredObjectReferenceMapper storedObjectReferenceMapper,
                                               IdGenerator idGenerator) {
        this.storedObjectReferenceMapper = storedObjectReferenceMapper;
        this.idGenerator = idGenerator;
    }

    @Override
    public StoredObjectReference save(StoredObjectReference storedObjectReference) {
        StoredObjectReferenceDO dataObject = toDataObject(storedObjectReference);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId(BIZ_TAG));
            storedObjectReferenceMapper.insert(dataObject);
        } else {
            storedObjectReferenceMapper.updateById(dataObject);
        }
        return toDomain(dataObject);
    }

    @Override
    public void deleteByObjectIdAndOwner(Long objectId, String ownerType, String ownerId) {
        storedObjectReferenceMapper.delete(Wrappers.<StoredObjectReferenceDO>lambdaQuery()
                .eq(StoredObjectReferenceDO::getObjectId, objectId)
                .eq(StoredObjectReferenceDO::getOwnerType, ownerType)
                .eq(StoredObjectReferenceDO::getOwnerId, ownerId));
    }

    @Override
    public boolean existsByObjectIdAndOwner(Long objectId, String ownerType, String ownerId) {
        return storedObjectReferenceMapper.selectCount(Wrappers.<StoredObjectReferenceDO>lambdaQuery()
                .eq(StoredObjectReferenceDO::getObjectId, objectId)
                .eq(StoredObjectReferenceDO::getOwnerType, ownerType)
                .eq(StoredObjectReferenceDO::getOwnerId, ownerId)) > 0;
    }

    @Override
    public boolean existsByObjectId(Long objectId) {
        return storedObjectReferenceMapper.selectCount(Wrappers.<StoredObjectReferenceDO>lambdaQuery()
                .eq(StoredObjectReferenceDO::getObjectId, objectId)) > 0;
    }

    private StoredObjectReferenceDO toDataObject(StoredObjectReference storedObjectReference) {
        return new StoredObjectReferenceDO(storedObjectReference.getId(), storedObjectReference.getObjectId(),
                storedObjectReference.getOwnerType(), storedObjectReference.getOwnerId(),
                storedObjectReference.getCreatedAt());
    }

    private StoredObjectReference toDomain(StoredObjectReferenceDO dataObject) {
        return new StoredObjectReference(dataObject.getId(), dataObject.getObjectId(), dataObject.getOwnerType(),
                dataObject.getOwnerId(), dataObject.getCreatedAt());
    }
}
