package com.github.thundax.bacon.auth.interfaces.response;

import com.github.thundax.bacon.auth.api.dto.UserLoginDTO;

public record UserLoginResponse(String accessToken, String refreshToken, String tokenType, long expiresIn,
                                String sessionId, Long userId, Long tenantId, Boolean needChangePassword) {

    public static UserLoginResponse from(UserLoginDTO dto) {
        return new UserLoginResponse(dto.getAccessToken(), dto.getRefreshToken(), dto.getTokenType(),
                dto.getExpiresIn(), dto.getSessionId(), dto.getUserId(), dto.getTenantId(),
                dto.getNeedChangePassword());
    }
}
