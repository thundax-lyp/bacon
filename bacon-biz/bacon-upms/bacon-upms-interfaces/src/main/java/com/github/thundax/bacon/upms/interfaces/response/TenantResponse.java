package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;

/**
 * 租户查询响应对象。
 */
public record TenantResponse(
        /** 租户主键。 */
        String id,
        /** 租户名称。 */
        String name,
        /** 租户状态。 */
        String status) {

    public static TenantResponse from(TenantDTO dto) {
        String tenantId = dto.getId() == null ? null : dto.getId().value();
        return new TenantResponse(tenantId, dto.getName(), dto.getStatus());
    }
}
