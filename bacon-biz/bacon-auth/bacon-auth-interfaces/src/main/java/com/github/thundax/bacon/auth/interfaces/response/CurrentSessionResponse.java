package com.github.thundax.bacon.auth.interfaces.response;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionDTO;
import java.time.Instant;

/**
 * 当前会话响应对象。
 */
public record CurrentSessionResponse(
        /** 会话标识。 */
        String sessionId,
        /** 所属租户编号。 */
        String tenantNo,
        /** 用户主键。 */
        String userId,
        /** 身份标识类型。 */
        String identityType,
        /** 登录方式类型。 */
        String loginType,
        /** 会话状态。 */
        String sessionStatus,
        /** 签发时间。 */
        Instant issuedAt,
        /** 最后访问时间。 */
        Instant lastAccessTime,
        /** 过期时间。 */
        Instant expireAt) {

    public static CurrentSessionResponse from(CurrentSessionDTO dto) {
        return new CurrentSessionResponse(dto.getSessionId(), dto.getTenantNo(), dto.getUserId(),
                dto.getIdentityType(), dto.getLoginType(), dto.getSessionStatus(), dto.getIssuedAt(),
                dto.getLastAccessTime(), dto.getExpireAt());
    }
}
