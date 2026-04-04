package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseLongId;

public final class TenantId extends BaseLongId {

    private TenantId(Long value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static TenantId of(Long value) {
        return new TenantId(value);
    }

    public static TenantId of(String value) {
        return new TenantId(Long.parseLong(value));
    }
}
