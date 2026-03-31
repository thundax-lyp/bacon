package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.DepartmentDTO;

/**
 * 部门查询响应对象。
 */
public record DepartmentResponse(
        /** 部门主键。 */
        Long id,
        /** 所属租户编号。 */
        String tenantId,
        /** 部门编码。 */
        String code,
        /** 部门名称。 */
        String name,
        /** 父部门主键，根部门固定为 0。 */
        Long parentId,
        /** 部门负责人用户主键。 */
        String leaderUserId,
        /** 部门状态。 */
        String status) {

    public static DepartmentResponse from(DepartmentDTO dto) {
        return new DepartmentResponse(dto.getId(), dto.getTenantId(), dto.getCode(), dto.getName(), dto.getParentId(),
                dto.getLeaderUserId(), dto.getStatus());
    }
}
