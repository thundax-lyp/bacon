package com.github.thundax.bacon.upms.api.response;

import java.time.Instant;

public record TenantFacadeResponse(String name, String code, String status, Instant expiredAt) {}
