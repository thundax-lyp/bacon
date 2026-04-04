package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseLongId;

public final class UserId extends BaseLongId {

    private UserId(Long value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserId of(Long value) {
        return new UserId(value);
    }

    public static UserId of(String value) {
        return new UserId(Long.parseLong(value));
    }
}
