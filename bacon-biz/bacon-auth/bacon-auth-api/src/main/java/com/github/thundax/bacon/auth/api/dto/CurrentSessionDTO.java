package com.github.thundax.bacon.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 当前会话传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentSessionDTO {

    /** 会话标识。 */
    private String sessionId;
    /** 所属租户编号。 */
    private String tenantId;
    /** 用户主键。 */
    private String userId;
    /** 身份标识类型。 */
    private String identityType;
    /** 登录方式类型。 */
    private String loginType;
    /** 会话状态。 */
    private String sessionStatus;
    /** 签发时间。 */
    private Instant issuedAt;
    /** 最后访问时间。 */
    private Instant lastAccessTime;
    /** 过期时间。 */
    private Instant expireAt;
}
