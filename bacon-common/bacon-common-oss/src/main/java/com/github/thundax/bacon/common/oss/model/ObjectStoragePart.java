package com.github.thundax.bacon.common.oss.model;

/**
 * 对象存储分片描述。
 */
public record ObjectStoragePart(
        /** 分片序号。 */
        Integer partNumber,
        /** 分片校验标识。 */
        String etag) {}
