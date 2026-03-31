package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseId;

public final class UserId extends BaseId<Long> {

    private UserId(Long value) {
        super(value, Long.class);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserId of(Long value) {
        return new UserId(value);
    }
}
