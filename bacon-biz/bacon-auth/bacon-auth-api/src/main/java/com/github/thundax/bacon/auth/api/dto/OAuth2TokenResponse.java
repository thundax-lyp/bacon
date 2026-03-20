package com.github.thundax.bacon.auth.api.dto;

public record OAuth2TokenResponse(
        String access_token,
        String token_type,
        long expires_in,
        String refresh_token,
        String scope) {
}
