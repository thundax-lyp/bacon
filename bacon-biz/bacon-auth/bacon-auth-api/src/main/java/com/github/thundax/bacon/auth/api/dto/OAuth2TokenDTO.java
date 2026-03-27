package com.github.thundax.bacon.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OAuth2 token 传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2TokenDTO {

    /** 访问令牌。 */
    @JsonProperty("access_token")
    private String accessToken;
    /** 令牌类型。 */
    @JsonProperty("token_type")
    private String tokenType;
    /** 有效期秒数。 */
    @JsonProperty("expires_in")
    private long expiresIn;
    /** 刷新令牌。 */
    @JsonProperty("refresh_token")
    private String refreshToken;
    /** 授权范围。 */
    private String scope;
}
