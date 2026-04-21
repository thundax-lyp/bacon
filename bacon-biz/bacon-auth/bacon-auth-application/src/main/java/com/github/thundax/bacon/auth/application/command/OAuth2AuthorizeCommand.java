package com.github.thundax.bacon.auth.application.command;

public record OAuth2AuthorizeCommand(
        String accessToken,
        String clientId,
        String redirectUri,
        String scope,
        String state,
        String codeChallenge,
        String codeChallengeMethod) {}
