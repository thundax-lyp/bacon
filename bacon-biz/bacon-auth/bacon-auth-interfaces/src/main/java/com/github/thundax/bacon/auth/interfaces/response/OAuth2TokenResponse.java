package com.github.thundax.bacon.auth.interfaces.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.thundax.bacon.auth.application.dto.OAuth2TokenDTO;

/**
 * OAuth2 token 响应对象。
 */
public record OAuth2TokenResponse(
        /** 访问令牌。 */
        @JsonProperty("access_token") String accessToken,
        /** 令牌类型。 */
        @JsonProperty("token_type") String tokenType,
        /** 有效期秒数。 */
        @JsonProperty("expires_in") long expiresIn,
        /** 刷新令牌。 */
        @JsonProperty("refresh_token") String refreshToken,
        /** 授权范围。 */
        String scope) {

    public static OAuth2TokenResponse from(OAuth2TokenDTO dto) {
        return new OAuth2TokenResponse(
                dto.getAccessToken(), dto.getTokenType(), dto.getExpiresIn(), dto.getRefreshToken(), dto.getScope());
    }
}
