package com.github.thundax.bacon.auth.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseStringId;

public final class ClientCode extends BaseStringId {

    private ClientCode(String value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ClientCode of(String value) {
        return new ClientCode(value);
    }
}
