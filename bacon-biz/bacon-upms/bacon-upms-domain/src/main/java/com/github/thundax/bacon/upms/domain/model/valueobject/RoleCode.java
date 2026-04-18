package com.github.thundax.bacon.upms.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;

public record RoleCode(String value) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static RoleCode of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("roleCode must not be blank");
        }
        return new RoleCode(value.trim());
    }
}
