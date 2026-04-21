package com.github.thundax.bacon.auth.application.command;

public record SessionInvalidateTenantCommand(Long tenantId, String reason) {}
