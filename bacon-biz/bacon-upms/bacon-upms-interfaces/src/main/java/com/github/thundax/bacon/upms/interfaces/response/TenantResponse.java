package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.TenantDTO;

/**
 * 租户查询响应对象。
 */
public record TenantResponse(
        /** 租户主键。 */
        Long id,
        /** 业务租户标识。 */
        Long tenantId,
        /** 租户编码。 */
        String code,
        /** 租户名称。 */
        String name,
        /** 租户状态。 */
        String status) {

    public static TenantResponse from(TenantDTO dto) {
        return new TenantResponse(dto.getId(), dto.getTenantId(), dto.getCode(), dto.getName(), dto.getStatus());
    }
}
