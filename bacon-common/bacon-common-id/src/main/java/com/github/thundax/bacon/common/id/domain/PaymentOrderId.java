package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseLongId;

public final class PaymentOrderId extends BaseLongId {

    private PaymentOrderId(Long value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PaymentOrderId of(Long value) {
        return new PaymentOrderId(value);
    }
}
