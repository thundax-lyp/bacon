package com.github.thundax.bacon.storage.application.query;

import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectNo;

/**
 * 查询存储对象详情条件。
 */
public record StoredObjectGetQuery(StoredObjectNo storedObjectNo) {}
