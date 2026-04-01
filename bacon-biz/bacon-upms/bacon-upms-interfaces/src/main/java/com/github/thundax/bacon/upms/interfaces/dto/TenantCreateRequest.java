package com.github.thundax.bacon.upms.interfaces.dto;

import java.time.Instant;

public record TenantCreateRequest(String tenantId, String name, String tenantCode, Instant expiredAt) {
}
