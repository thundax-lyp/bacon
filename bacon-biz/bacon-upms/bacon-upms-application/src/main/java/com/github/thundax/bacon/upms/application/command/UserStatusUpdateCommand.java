package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;

public record UserStatusUpdateCommand(UserId userId, UserStatus status) {}
