package com.github.thundax.bacon.upms.interfaces.request;

import java.time.Instant;

public record TenantUpdateRequest(String name, String tenantCode, Instant expiredAt) {}
