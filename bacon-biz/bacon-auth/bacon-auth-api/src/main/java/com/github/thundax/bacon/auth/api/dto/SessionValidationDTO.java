package com.github.thundax.bacon.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 会话校验结果对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionValidationDTO {

    /** 是否有效。 */
    private boolean valid;
    /** 所属租户编号。 */
    private String tenantId;
    /** 用户主键。 */
    private String userId;
    /** 会话标识。 */
    private String sessionId;
    /** 身份标识值。 */
    private String identityId;
    /** 身份标识类型。 */
    private String identityType;
    /** 过期时间。 */
    private Instant expireAt;
}
