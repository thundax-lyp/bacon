package com.github.thundax.bacon.storage.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectReferenceRepository;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectReference;
import com.github.thundax.bacon.storage.infra.persistence.dataobject.StoredObjectReferenceDO;
import com.github.thundax.bacon.storage.infra.persistence.mapper.StoredObjectReferenceMapper;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DuplicateKeyException;

@Repository
public class StoredObjectReferenceRepositoryImpl implements StoredObjectReferenceRepository {

    private final StoredObjectReferenceMapper storedObjectReferenceMapper;

    public StoredObjectReferenceRepositoryImpl(StoredObjectReferenceMapper storedObjectReferenceMapper) {
        this.storedObjectReferenceMapper = storedObjectReferenceMapper;
    }

    @Override
    public boolean saveIfAbsent(StoredObjectReference storedObjectReference) {
        StoredObjectReferenceDO dataObject = toDataObject(storedObjectReference);
        try {
            storedObjectReferenceMapper.insert(dataObject);
            return true;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }

    @Override
    public boolean deleteByObjectIdAndOwner(StoredObjectId objectId, String ownerType, String ownerId) {
        return storedObjectReferenceMapper.delete(Wrappers.<StoredObjectReferenceDO>lambdaQuery()
                .eq(StoredObjectReferenceDO::getObjectId, objectId)
                .eq(StoredObjectReferenceDO::getOwnerType, ownerType)
                .eq(StoredObjectReferenceDO::getOwnerId, ownerId)) > 0;
    }

    @Override
    public boolean existsByObjectIdAndOwner(StoredObjectId objectId, String ownerType, String ownerId) {
        return storedObjectReferenceMapper.selectCount(Wrappers.<StoredObjectReferenceDO>lambdaQuery()
                .eq(StoredObjectReferenceDO::getObjectId, objectId)
                .eq(StoredObjectReferenceDO::getOwnerType, ownerType)
                .eq(StoredObjectReferenceDO::getOwnerId, ownerId)) > 0;
    }

    @Override
    public boolean existsByObjectId(StoredObjectId objectId) {
        return storedObjectReferenceMapper.selectCount(Wrappers.<StoredObjectReferenceDO>lambdaQuery()
                .eq(StoredObjectReferenceDO::getObjectId, objectId)) > 0;
    }

    private StoredObjectReferenceDO toDataObject(StoredObjectReference storedObjectReference) {
        return new StoredObjectReferenceDO(storedObjectReference.getObjectId(),
                storedObjectReference.getOwnerType(), storedObjectReference.getOwnerId());
    }
}
