package com.github.thundax.bacon.upms.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;

public record TenantCode(String value) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static TenantCode of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tenantCode must not be blank");
        }
        return new TenantCode(value.trim());
    }
}
