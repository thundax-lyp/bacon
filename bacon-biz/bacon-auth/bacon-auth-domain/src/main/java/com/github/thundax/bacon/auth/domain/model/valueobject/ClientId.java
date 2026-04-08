package com.github.thundax.bacon.auth.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseStringId;

public final class ClientId extends BaseStringId {

    private ClientId(String value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ClientId of(String value) {
        return new ClientId(value);
    }
}
