package com.github.thundax.bacon.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户令牌刷新结果对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenRefreshDTO {

    /** 访问令牌。 */
    private String accessToken;
    /** 刷新令牌。 */
    private String refreshToken;
    /** 令牌类型。 */
    private String tokenType;
    /** 有效期秒数。 */
    private long expiresIn;
    /** 会话标识。 */
    private String sessionId;
}
