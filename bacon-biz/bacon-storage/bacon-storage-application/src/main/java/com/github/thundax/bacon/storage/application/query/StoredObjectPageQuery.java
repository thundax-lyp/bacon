package com.github.thundax.bacon.storage.application.query;

import com.github.thundax.bacon.common.application.page.PageQuery;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;

/**
 * 存储对象分页查询条件。
 */
public class StoredObjectPageQuery extends PageQuery {

    private final StorageType storageType;
    private final StoredObjectStatus objectStatus;
    private final StoredObjectReferenceStatus referenceStatus;
    private final String originalFilename;
    private final String objectKey;

    public StoredObjectPageQuery(
            StorageType storageType,
            StoredObjectStatus objectStatus,
            StoredObjectReferenceStatus referenceStatus,
            String originalFilename,
            String objectKey,
            Integer pageNo,
            Integer pageSize) {
        super(pageNo, pageSize);
        this.storageType = storageType;
        this.objectStatus = objectStatus;
        this.referenceStatus = referenceStatus;
        this.originalFilename = originalFilename;
        this.objectKey = objectKey;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public StoredObjectStatus getObjectStatus() {
        return objectStatus;
    }

    public StoredObjectReferenceStatus getReferenceStatus() {
        return referenceStatus;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getObjectKey() {
        return objectKey;
    }
}
