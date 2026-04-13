package com.github.thundax.bacon.storage.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 存储对象引用关系实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoredObjectReference {

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
        return new StoredObjectReference(objectId, ownerType, ownerId);
    }

    public static StoredObjectReference reconstruct(StoredObjectId objectId, String ownerType, String ownerId) {
        return new StoredObjectReference(objectId, ownerType, ownerId);
    }
}
