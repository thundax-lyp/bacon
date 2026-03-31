package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseId;

public final class OrderId extends BaseId<Long> {

    private OrderId(Long value) {
        super(value, Long.class);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OrderId of(Long value) {
        return new OrderId(value);
    }
}
