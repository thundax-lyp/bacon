package com.github.thundax.bacon.auth.interfaces.response;

import com.github.thundax.bacon.auth.api.dto.UserLoginDTO;

/**
 * 用户登录响应对象。
 */
public record UserLoginResponse(
        /** 访问令牌。 */
        String accessToken,
        /** 刷新令牌。 */
        String refreshToken,
        /** 令牌类型。 */
        String tokenType,
        /** 有效期秒数。 */
        long expiresIn,
        /** 会话标识。 */
        String sessionId,
        /** 用户主键。 */
        Long userId,
        /** 所属租户主键。 */
        Long tenantId,
        /** 是否需要修改密码。 */
        Boolean needChangePassword) {

    public static UserLoginResponse from(UserLoginDTO dto) {
        return new UserLoginResponse(dto.getAccessToken(), dto.getRefreshToken(), dto.getTokenType(),
                dto.getExpiresIn(), dto.getSessionId(), dto.getUserId(), dto.getTenantId(),
                dto.getNeedChangePassword());
    }
}
