package com.github.thundax.bacon.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录结果对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {

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
    /** 用户主键。 */
    private Long userId;
    /** 所属租户编号。 */
    private String tenantNo;
    /** 是否需要修改密码。 */
    private Boolean needChangePassword;
}
