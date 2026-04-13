package com.github.thundax.bacon.upms.application.codec;

import com.github.thundax.bacon.common.id.domain.ResourceId;

public final class ResourceIdCodec {

    private ResourceIdCodec() {}

    public static ResourceId toDomain(Long value) {
        return value == null ? null : ResourceId.of(value);
    }

    public static Long toValue(ResourceId value) {
        return value == null ? null : value.value();
    }
}
