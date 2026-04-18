package com.github.thundax.bacon.upms.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;

public record PostCode(String value) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static PostCode of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("postCode must not be blank");
        }
        return new PostCode(value.trim());
    }
}
