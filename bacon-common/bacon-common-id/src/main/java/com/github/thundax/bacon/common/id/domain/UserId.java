package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseId;

public final class UserId extends BaseId<String> {

    private UserId(String value) {
        super(value, String.class);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserId of(String value) {
        return new UserId(value);
    }
}
