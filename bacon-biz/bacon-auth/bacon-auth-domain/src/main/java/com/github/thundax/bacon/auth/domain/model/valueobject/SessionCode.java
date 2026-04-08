package com.github.thundax.bacon.auth.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseStringId;

public final class SessionCode extends BaseStringId {

    private SessionCode(String value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SessionCode of(String value) {
        return new SessionCode(value);
    }
}
