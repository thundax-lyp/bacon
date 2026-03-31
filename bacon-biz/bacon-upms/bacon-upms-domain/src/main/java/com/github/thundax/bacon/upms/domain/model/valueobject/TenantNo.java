package com.github.thundax.bacon.upms.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 租户编号值对象。
 */
@EqualsAndHashCode
@ToString
public final class TenantNo {

    private final String value;

    public TenantNo(String value) {
        String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty()) {
            throw new IllegalArgumentException("tenantNo cannot be blank");
        }
        this.value = normalized;
    }

    public String value() {
        return value;
    }
}
