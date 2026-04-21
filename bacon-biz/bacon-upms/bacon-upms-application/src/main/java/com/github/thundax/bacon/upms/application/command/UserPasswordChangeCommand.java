package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.domain.UserId;

public record UserPasswordChangeCommand(UserId userId, String oldPassword, String newPassword) {}
