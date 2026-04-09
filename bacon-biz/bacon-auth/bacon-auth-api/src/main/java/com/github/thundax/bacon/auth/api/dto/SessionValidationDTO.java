package com.github.thundax.bacon.auth.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Long tenantId;
    /** 用户主键。 */
    private Long userId;
    /** 会话标识。 */
    private String sessionId;
    /** 身份标识值。 */
    private Long identityId;
    /** 身份标识类型。 */
    private String identityType;
    /** 过期时间。 */
    private Instant expireAt;
}
