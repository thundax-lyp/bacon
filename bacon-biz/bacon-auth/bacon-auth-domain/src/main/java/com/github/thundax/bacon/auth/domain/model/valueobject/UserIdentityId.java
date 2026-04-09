package com.github.thundax.bacon.auth.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseLongId;

public final class UserIdentityId extends BaseLongId {

    private UserIdentityId(Long value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserIdentityId of(Long value) {
        return new UserIdentityId(value);
    }

    public static UserIdentityId of(String value) {
        return new UserIdentityId(Long.parseLong(value));
    }
}
