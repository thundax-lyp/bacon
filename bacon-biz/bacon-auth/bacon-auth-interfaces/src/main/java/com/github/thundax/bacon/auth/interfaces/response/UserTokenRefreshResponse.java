package com.github.thundax.bacon.auth.interfaces.response;

import com.github.thundax.bacon.auth.api.dto.UserTokenRefreshDTO;

public record UserTokenRefreshResponse(String accessToken, String refreshToken, String tokenType, long expiresIn,
                                       String sessionId) {

    public static UserTokenRefreshResponse from(UserTokenRefreshDTO dto) {
        return new UserTokenRefreshResponse(dto.getAccessToken(), dto.getRefreshToken(), dto.getTokenType(),
                dto.getExpiresIn(), dto.getSessionId());
    }
}
