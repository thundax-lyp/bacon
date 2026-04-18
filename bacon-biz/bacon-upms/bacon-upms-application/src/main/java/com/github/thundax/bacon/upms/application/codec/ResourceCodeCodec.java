package com.github.thundax.bacon.upms.application.codec;

import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;

public final class ResourceCodeCodec {

    private ResourceCodeCodec() {}

    public static ResourceCode toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return ResourceCode.of(value.trim());
    }

    public static String toValue(ResourceCode value) {
        return value == null ? null : value.value();
    }
}
