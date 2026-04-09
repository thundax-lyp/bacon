package com.github.thundax.bacon.common.commerce.identifier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseLongId;

public final class SkuId extends BaseLongId {

    private SkuId(Long value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SkuId of(Long value) {
        return new SkuId(value);
    }
}
