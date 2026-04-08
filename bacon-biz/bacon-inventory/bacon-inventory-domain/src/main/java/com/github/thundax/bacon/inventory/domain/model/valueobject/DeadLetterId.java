package com.github.thundax.bacon.inventory.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseLongId;

/**
 * 死信记录主键。
 */
public final class DeadLetterId extends BaseLongId {

    private DeadLetterId(Long value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DeadLetterId of(Long value) {
        return new DeadLetterId(value);
    }
}
