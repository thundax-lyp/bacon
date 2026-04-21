package com.github.thundax.bacon.storage.application.command;

import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectNo;

/**
 * 存储对象删除命令。
 */
public record StoredObjectDeleteCommand(StoredObjectNo storedObjectNo) {}
