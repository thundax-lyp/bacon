package com.github.thundax.bacon.order.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseLongId;

public final class OrderOutboxDeadLetterId extends BaseLongId {

    private OrderOutboxDeadLetterId(Long value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static OrderOutboxDeadLetterId of(Long value) {
        return new OrderOutboxDeadLetterId(value);
    }
}
