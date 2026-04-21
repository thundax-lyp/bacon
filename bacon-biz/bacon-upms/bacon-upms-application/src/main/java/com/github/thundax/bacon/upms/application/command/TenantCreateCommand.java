package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.domain.model.valueobject.TenantCode;
import java.time.Instant;

public record TenantCreateCommand(String name, TenantCode code, Instant expiredAt) {}
