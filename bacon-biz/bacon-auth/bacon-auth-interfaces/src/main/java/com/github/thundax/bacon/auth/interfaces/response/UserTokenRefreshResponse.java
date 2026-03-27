package com.github.thundax.bacon.auth.interfaces.response;

import com.github.thundax.bacon.auth.api.dto.UserTokenRefreshDTO;

/**
 * 用户令牌刷新响应对象。
 */
public record UserTokenRefreshResponse(
        /** 访问令牌。 */
        String accessToken,
        /** 刷新令牌。 */
        String refreshToken,
        /** 令牌类型。 */
        String tokenType,
        /** 有效期秒数。 */
        long expiresIn,
        /** 会话标识。 */
        String sessionId) {

    public static UserTokenRefreshResponse from(UserTokenRefreshDTO dto) {
        return new UserTokenRefreshResponse(dto.getAccessToken(), dto.getRefreshToken(), dto.getTokenType(),
                dto.getExpiresIn(), dto.getSessionId());
    }
}
