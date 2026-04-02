package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseStringId;

public final class StoredObjectId extends BaseStringId {

    private StoredObjectId(String value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StoredObjectId of(String value) {
        return new StoredObjectId(value);
    }
}
