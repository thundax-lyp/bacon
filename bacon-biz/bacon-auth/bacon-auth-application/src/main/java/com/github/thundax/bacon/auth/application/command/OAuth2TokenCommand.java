package com.github.thundax.bacon.auth.application.command;

public record OAuth2TokenCommand(
        String grantType,
        String code,
        String redirectUri,
        String clientId,
        String clientSecret,
        String codeVerifier,
        String refreshToken) {}
