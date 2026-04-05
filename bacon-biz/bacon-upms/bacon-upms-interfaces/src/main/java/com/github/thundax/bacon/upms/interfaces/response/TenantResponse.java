package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;
import java.time.Instant;

/**
 * 租户查询响应对象。
 */
public record TenantResponse(
        /** 租户主键。 */
        String id,
        /** 租户名称。 */
        String name,
        /** 稳定业务编码。 */
        String tenantCode,
        /** 租户状态。 */
        String status,
        /** 过期时间。 */
        Instant expiredAt) {

    public static TenantResponse from(TenantDTO dto) {
        String tenantId = dto.getId() == null ? null : String.valueOf(dto.getId().value());
        return new TenantResponse(tenantId, dto.getName(), dto.getTenantCode(), dto.getStatus(), dto.getExpiredAt());
    }
}
