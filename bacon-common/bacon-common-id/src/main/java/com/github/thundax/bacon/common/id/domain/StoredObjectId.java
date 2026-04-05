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

    public static StoredObjectId of(String value) {
        return new StoredObjectId(parse(value));
    }

    public String externalValue() {
        return EXTERNAL_PREFIX + value();
    }

    private static Long parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("id cannot be blank");
        }
        String normalizedValue = value.startsWith(EXTERNAL_PREFIX) ? value.substring(EXTERNAL_PREFIX.length()) : value;
        try {
            return Long.parseLong(normalizedValue);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("invalid stored object id: " + value, ex);
        }
    }
}
