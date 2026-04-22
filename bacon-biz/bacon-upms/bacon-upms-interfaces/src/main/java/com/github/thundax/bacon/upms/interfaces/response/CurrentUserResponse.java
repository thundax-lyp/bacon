package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.application.dto.UserDTO;

/**
 * 当前用户响应对象。
 */
public record CurrentUserResponse(
        Long id,
        String account,
        String name,
        String avatarStoredObjectNo,
        String phone,
        String departmentCode,
        String avatarUrl,
        String status) {

    public static CurrentUserResponse from(UserDTO dto, String departmentCode) {
        return new CurrentUserResponse(
                dto.getId(),
                dto.getAccount(),
                dto.getName(),
                dto.getAvatarStoredObjectNo(),
                dto.getPhone(),
                departmentCode,
                dto.getAvatarUrl(),
                dto.getStatus());
    }
}
