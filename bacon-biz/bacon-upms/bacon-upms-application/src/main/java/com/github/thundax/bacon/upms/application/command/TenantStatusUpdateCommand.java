package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.enums.TenantStatus;

public record TenantStatusUpdateCommand(TenantId tenantId, TenantStatus status) {}
