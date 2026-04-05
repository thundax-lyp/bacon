package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseLongId;

public final class StoredObjectId extends BaseLongId {

    private static final String EXTERNAL_PREFIX = "O";

    private StoredObjectId(Long value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StoredObjectId of(Long value) {
        return new StoredObjectId(value);
    }

    public String externalValue() {
        return EXTERNAL_PREFIX + value();
    }

}
