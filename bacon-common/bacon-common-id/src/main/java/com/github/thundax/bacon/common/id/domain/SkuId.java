package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseId;

public final class SkuId extends BaseId<Long> {

    private SkuId(Long value) {
        super(value, Long.class);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SkuId of(Long value) {
        return new SkuId(value);
    }
}
