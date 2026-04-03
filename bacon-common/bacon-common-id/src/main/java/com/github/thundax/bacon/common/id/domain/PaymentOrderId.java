package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseStringId;

public final class PaymentOrderId extends BaseStringId {

    private PaymentOrderId(String value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PaymentOrderId of(String value) {
        return new PaymentOrderId(value);
    }
}
