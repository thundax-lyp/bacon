package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseStringId;

public final class UserCredentialId extends BaseStringId {

    private UserCredentialId(String value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserCredentialId of(String value) {
        return new UserCredentialId(value);
    }
}
