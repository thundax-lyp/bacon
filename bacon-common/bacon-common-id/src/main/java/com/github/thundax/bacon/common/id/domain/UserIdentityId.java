package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseStringId;

public final class UserIdentityId extends BaseStringId {

    private UserIdentityId(String value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserIdentityId of(String value) {
        return new UserIdentityId(value);
    }
}
