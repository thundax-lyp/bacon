package com.github.thundax.bacon.upms.interfaces.request;

import java.time.Instant;

public record TenantCreateRequest(Long tenantId, String name, String code, Instant expiredAt) {}
