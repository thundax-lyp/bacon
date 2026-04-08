package com.github.thundax.bacon.common.id.mapper;

import com.github.thundax.bacon.common.id.domain.SkuId;

public final class SkuIdMapper {

    private SkuIdMapper() {
    }

    public static SkuId toDomain(Long value) {
        if (value == null) {
            return null;
        }
        return SkuId.of(value);
    }
}
