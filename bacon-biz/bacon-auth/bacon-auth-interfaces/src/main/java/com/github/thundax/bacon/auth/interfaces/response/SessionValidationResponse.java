package com.github.thundax.bacon.auth.interfaces.response;

import com.github.thundax.bacon.auth.api.dto.SessionValidationDTO;
import java.time.Instant;

/**
 * 访问令牌校验响应对象。
 */
public record SessionValidationResponse(
        /** 是否有效。 */
        boolean valid,
        /** 所属租户编号。 */
        Long tenantId,
        /** 用户主键。 */
        Long userId,
        /** 会话标识。 */
        String sessionId,
        /** 身份标识值。 */
        Long identityId,
        /** 身份标识类型。 */
        String identityType,
        /** 过期时间。 */
        Instant expireAt) {

    public static SessionValidationResponse from(SessionValidationDTO dto) {
        if (dto == null) {
            return null;
        }
        return new SessionValidationResponse(
                dto.isValid(),
                dto.getTenantId(),
                dto.getUserId(),
                dto.getSessionId(),
                dto.getIdentityId(),
                dto.getIdentityType(),
                dto.getExpireAt());
    }
}
