package com.github.thundax.bacon.auth.api.dto;

public record UserTokenRefreshResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String sessionId) {
}
