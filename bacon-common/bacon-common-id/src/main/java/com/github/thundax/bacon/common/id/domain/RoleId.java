package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseStringId;

public final class RoleId extends BaseStringId {

    private RoleId(String value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RoleId of(String value) {
        return new RoleId(value);
    }
}
