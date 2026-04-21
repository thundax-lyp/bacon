package com.github.thundax.bacon.auth.application.command;

public record OAuth2RevokeCommand(String token, String clientId, String clientSecret) {}
