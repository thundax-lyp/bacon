package com.github.thundax.bacon.storage.interfaces.dto;

/**
 * 存储对象引用请求。
 */
public record StoredObjectReferenceRequest(
        String ownerType,
        String ownerId) {
}
