package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseId;

public final class TenantId extends BaseId<String> {

    private TenantId(String value) {
        super(value, String.class);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static TenantId of(String value) {
        return new TenantId(value);
    }
}
