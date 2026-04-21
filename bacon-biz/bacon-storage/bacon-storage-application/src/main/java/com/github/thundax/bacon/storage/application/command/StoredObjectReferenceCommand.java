package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectNo;

/**
 * 存储对象引用命令。
 */
public record StoredObjectReferenceCommand(StoredObjectNo storedObjectNo, String ownerType, String ownerId) {}
