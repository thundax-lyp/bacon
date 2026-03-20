package com.github.thundax.bacon.auth.api.dto;

public record UserLoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String sessionId,
        Long userId,
        Long tenantId,
        Boolean needChangePassword) {
}
