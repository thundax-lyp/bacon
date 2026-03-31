package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.PostDTO;

/**
 * 岗位查询响应对象。
 */
public record PostResponse(
        /** 岗位主键。 */
        Long id,
        /** 所属租户编号。 */
        String tenantId,
        /** 岗位编码。 */
        String code,
        /** 岗位名称。 */
        String name,
        /** 所属部门主键。 */
        Long departmentId,
        /** 岗位状态。 */
        String status) {

    public static PostResponse from(PostDTO dto) {
        return new PostResponse(dto.getId(), dto.getTenantId(), dto.getCode(), dto.getName(),
                dto.getDepartmentId(), dto.getStatus());
    }
}
