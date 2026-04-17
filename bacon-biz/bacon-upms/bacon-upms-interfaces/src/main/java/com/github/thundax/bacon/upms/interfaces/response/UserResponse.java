package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.UserDTO;

/**
 * 用户查询响应对象。
 */
public record UserResponse(
        /** 用户主键。 */
        Long id,
        /** 登录账号。 */
        String account,
        /** 用户名称。 */
        String name,
        /** 头像存储对象编号。 */
        String avatarStoredObjectNo,
        /** 手机号。 */
        String phone,
        /** 所属部门主键。 */
        Long departmentId,
        /** 头像访问地址。 */
        String avatarUrl,
        /** 用户状态。 */
        String status) {

    public static UserResponse from(UserDTO dto) {
        return new UserResponse(
                dto.getId(),
                dto.getAccount(),
                dto.getName(),
                dto.getAvatarStoredObjectNo(),
                dto.getPhone(),
                dto.getDepartmentId(),
                dto.getAvatarUrl(),
                dto.getStatus());
    }
}
