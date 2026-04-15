package com.github.thundax.bacon.upms.application.assembler;

import com.github.thundax.bacon.upms.api.dto.UserDTO;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.domain.model.entity.User;

public final class UserAssembler {

    private UserAssembler() {}

    public static UserDTO toDto(User user, String account, String phone, String avatarUrl) {
        return new UserDTO(
                user.getId().value(),
                account,
                user.getName(),
                user.getAvatarObjectId() == null
                        ? null
                        : user.getAvatarObjectId().value(),
                phone,
                DepartmentIdCodec.toValue(user.getDepartmentId()),
                avatarUrl,
                user.getStatus().value());
    }
}
