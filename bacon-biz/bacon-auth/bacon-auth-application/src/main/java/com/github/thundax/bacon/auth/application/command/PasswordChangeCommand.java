package com.github.thundax.bacon.auth.application.command;

public record PasswordChangeCommand(
        String accessToken,
        String oldPassword,
        String newPassword) {}
