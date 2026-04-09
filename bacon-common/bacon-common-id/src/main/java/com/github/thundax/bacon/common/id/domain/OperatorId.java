package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseStringId;

public final class OperatorId extends BaseStringId {

    private OperatorId(String value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OperatorId of(String value) {
        return new OperatorId(value);
    }
}
