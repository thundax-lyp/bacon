package com.github.thundax.bacon.auth.application.command;

public record SessionInvalidateCommand(String sessionId, String reason) {}
