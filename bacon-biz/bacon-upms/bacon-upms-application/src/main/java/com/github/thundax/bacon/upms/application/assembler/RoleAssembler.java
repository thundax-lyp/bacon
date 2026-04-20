package com.github.thundax.bacon.upms.application.assembler;

import com.github.thundax.bacon.upms.application.codec.RoleCodeCodec;
import com.github.thundax.bacon.upms.application.codec.RoleIdCodec;
import com.github.thundax.bacon.upms.application.dto.RoleDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Role;

public final class RoleAssembler {

    private RoleAssembler() {}

    public static RoleDTO toDto(Role role) {
        return new RoleDTO(
                RoleIdCodec.toValue(role.getId()),
                RoleCodeCodec.toValue(role.getCode()),
                role.getName(),
                role.getRoleType() == null ? null : role.getRoleType().value(),
                role.getDataScopeType() == null ? null : role.getDataScopeType().value(),
                role.getStatus() == null ? null : role.getStatus().value());
    }
}
