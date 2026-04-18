package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import java.time.Instant;

/**
 * 租户查询响应对象。
 */
public record TenantResponse(
        /** 租户主键。 */
        Long id,
        /** 租户名称。 */
        String name,
        /** 稳定业务编码。 */
        String code,
        /** 租户状态。 */
        String status,
        /** 过期时间。 */
        Instant expiredAt) {

    public static TenantResponse from(TenantDTO dto) {
        return new TenantResponse(dto.getId(), dto.getName(), dto.getCode(), dto.getStatus(), dto.getExpiredAt());
    }
}
