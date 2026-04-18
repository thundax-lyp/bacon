package com.github.thundax.bacon.upms.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;

public record ResourceCode(String value) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ResourceCode of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("resourceCode must not be blank");
        }
        return new ResourceCode(value.trim());
    }
}
