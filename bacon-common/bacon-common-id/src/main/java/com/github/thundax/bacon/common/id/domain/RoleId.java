package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseId;

public final class RoleId extends BaseId<Long> {

    private RoleId(Long value) {
        super(value, Long.class);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RoleId of(Long value) {
        return new RoleId(value);
    }
}
