package com.github.thundax.bacon.storage.domain.model.valueobject;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import lombok.Getter;

/**
 * 存储对象引用关系值对象。
 */
@Getter
public class StoredObjectReference {

    /** 存储对象主键。 */
    private final StoredObjectId objectId;
    /** 引用方类型。 */
    private final String ownerType;
    /** 引用方业务主键。 */
    private final String ownerId;

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
        return new StoredObjectReference(objectId, ownerType, ownerId);
    }

    public StoredObjectReference(StoredObjectId objectId, String ownerType, String ownerId) {
        this.objectId = objectId;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
    }
}
