package com.github.thundax.bacon.auth.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseLongId;

public final class ClientId extends BaseLongId {

    private ClientId(Long value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ClientId of(Long value) {
        return new ClientId(value);
    }
}
