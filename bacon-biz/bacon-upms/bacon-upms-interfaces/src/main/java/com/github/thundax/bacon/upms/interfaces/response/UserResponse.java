package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.UserDTO;

/**
 * 用户查询响应对象。
 */
public record UserResponse(
        /** 用户主键。 */
        Long id,
        /** 所属租户主键。 */
        Long tenantId,
        /** 登录账号。 */
        String account,
        /** 用户名称。 */
        String name,
        /** 手机号。 */
        String phone,
        /** 所属部门主键。 */
        Long departmentId,
        /** 用户状态。 */
        String status,
        /** 逻辑删除标记。 */
        boolean deleted) {

    public static UserResponse from(UserDTO dto) {
        return new UserResponse(dto.getId(), dto.getTenantId(), dto.getAccount(), dto.getName(), dto.getPhone(),
                dto.getDepartmentId(), dto.getStatus(), dto.isDeleted());
    }
}
