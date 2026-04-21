package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.domain.UserId;

public record UserPasswordResetCommand(UserId userId, String newPassword) {}
