package com.github.thundax.bacon.storage.domain.model.entity;

import lombok.Getter;

/**
 * 存储对象引用关系实体。
 */
@Getter
public class StoredObjectReference {

    /** 主键。 */
    private Long id;
    /** 存储对象主键。 */
    private Long objectId;
    /** 引用方类型。 */
    private String ownerType;
    /** 引用方业务主键。 */
    private String ownerId;

    public StoredObjectReference(Long id, Long objectId, String ownerType, String ownerId) {
        this.id = id;
        this.objectId = objectId;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
    }
}
