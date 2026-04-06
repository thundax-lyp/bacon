package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.RoleDTO;

/**
 * 角色查询响应对象。
 */
public record RoleResponse(
        /** 角色主键。 */
        Long id,
        /** 所属租户编号。 */
        Long tenantId,
        /** 角色编码。 */
        String code,
        /** 角色名称。 */
        String name,
        /** 角色类型。 */
        String roleType,
        /** 数据范围类型。 */
        String dataScopeType,
        /** 角色状态。 */
        String status) {

    public static RoleResponse from(RoleDTO dto) {
        return new RoleResponse(dto.getId(), dto.getTenantId(), dto.getCode(), dto.getName(), dto.getRoleType(),
                dto.getDataScopeType(), dto.getStatus());
    }
}
