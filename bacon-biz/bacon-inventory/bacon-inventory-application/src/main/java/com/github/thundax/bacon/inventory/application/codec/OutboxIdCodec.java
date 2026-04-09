package com.github.thundax.bacon.inventory.application.codec;

import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;

public final class OutboxIdCodec {

    private OutboxIdCodec() {}

    public static OutboxId toDomain(Long value) {
        if (value == null) {
            return null;
        }
        return OutboxId.of(value);
    }

    public static Long toValue(OutboxId outboxId) {
        return outboxId == null ? null : outboxId.value();
    }
}
