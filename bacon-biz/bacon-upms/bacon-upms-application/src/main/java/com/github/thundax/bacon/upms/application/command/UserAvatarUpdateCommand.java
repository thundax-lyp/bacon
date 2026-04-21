package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.domain.UserId;
import java.io.InputStream;

public record UserAvatarUpdateCommand(
        UserId userId,
        String originalFilename,
        String contentType,
        Long size,
        InputStream inputStream) {}
