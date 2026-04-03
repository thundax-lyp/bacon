package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseStringId;

public final class OrderId extends BaseStringId {

    private OrderId(String value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OrderId of(String value) {
        return new OrderId(value);
    }
}
