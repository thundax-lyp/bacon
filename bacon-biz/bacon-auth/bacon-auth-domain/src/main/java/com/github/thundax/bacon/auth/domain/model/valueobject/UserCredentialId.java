package com.github.thundax.bacon.auth.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseLongId;

public final class UserCredentialId extends BaseLongId {

    private UserCredentialId(Long value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserCredentialId of(Long value) {
        return new UserCredentialId(value);
    }

    public static UserCredentialId of(String value) {
        return new UserCredentialId(Long.parseLong(value));
    }
}
