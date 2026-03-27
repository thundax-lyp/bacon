package com.github.thundax.bacon.storage.domain.model.entity;

import lombok.Getter;

/**
 * 存储对象引用关系实体。
 */
@Getter
public class StoredObjectReference {

    private final Long id;
    private final Long objectId;
    private final String ownerType;
    private final String ownerId;

    public StoredObjectReference(Long id, Long objectId, String ownerType, String ownerId) {
        this.id = id;
        this.objectId = objectId;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
    }
}
