package com.github.thundax.bacon.storage.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import lombok.Getter;

/**
 * 存储对象引用关系实体。
 */
@Getter
public class StoredObjectReference {

    /** 主键。 */
    private Long id;
    /** 存储对象主键。 */
    private StoredObjectId objectId;
    /** 引用方类型。 */
    private String ownerType;
    /** 引用方业务主键。 */
    private String ownerId;

    public static StoredObjectReference create(StoredObjectId objectId, String ownerType, String ownerId) {
        if (objectId == null) {
            throw new IllegalArgumentException("objectId must not be null");
        }
        if (ownerType == null || ownerType.isBlank()) {
            throw new IllegalArgumentException("ownerType must not be blank");
        }
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("ownerId must not be blank");
        }
        return new StoredObjectReference(null, objectId, ownerType, ownerId);
    }

    public StoredObjectReference(Long id, StoredObjectId objectId, String ownerType, String ownerId) {
        this.id = id;
        this.objectId = objectId;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
    }
}
