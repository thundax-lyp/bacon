package com.github.thundax.bacon.upms.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.regex.Pattern;

public record TenantCode(String value) {

    private static final Pattern PATTERN = Pattern.compile("[A-Z0-9_]+");

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static TenantCode of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("tenantCode must not be blank");
        }
        String normalizedValue = value.trim();
        if (!PATTERN.matcher(normalizedValue).matches()) {
            throw new IllegalArgumentException("tenantCode must match [A-Z0-9_]+");
        }
        return new TenantCode(normalizedValue);
    }
}
