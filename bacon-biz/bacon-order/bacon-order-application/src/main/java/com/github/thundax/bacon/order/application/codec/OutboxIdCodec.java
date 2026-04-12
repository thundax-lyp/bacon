package com.github.thundax.bacon.order.application.codec;

import com.github.thundax.bacon.order.domain.model.valueobject.OutboxId;

public final class OutboxIdCodec {

    private OutboxIdCodec() {}

    public static OutboxId toDomain(Long value) {
        return value == null ? null : OutboxId.of(value);
    }

    public static Long toValue(OutboxId value) {
        return value == null ? null : value.value();
    }
}
