package com.github.thundax.bacon.storage.domain.model.valueobject;

import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;

import java.util.List;

/**
 * 存储对象分页结果。
 */
public record StoredObjectPageResult(
        List<StoredObject> records,
        long total
) {
}
