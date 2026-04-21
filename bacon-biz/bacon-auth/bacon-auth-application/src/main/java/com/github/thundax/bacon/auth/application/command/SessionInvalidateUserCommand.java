package com.github.thundax.bacon.auth.application.command;

public record SessionInvalidateUserCommand(Long tenantId, Long userId, String reason) {}
