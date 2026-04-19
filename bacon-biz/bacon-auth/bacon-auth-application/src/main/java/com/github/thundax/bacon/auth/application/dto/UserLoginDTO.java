package com.github.thundax.bacon.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录结果应用层模型。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private String sessionId;
    private Long userId;
    private Long tenantId;
    private Boolean needChangePassword;
}
