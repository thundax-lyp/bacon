package com.github.thundax.bacon.upms.infra.persistence.assembler;

import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.RoleDO;

public final class RolePersistenceAssembler {

    private RolePersistenceAssembler() {}

    public static RoleDO toDataObject(Role role) {
        return new RoleDO(
                role.getId(),
                role.getTenantId(),
                role.getCode(),
                role.getName(),
                role.getRoleType() == null ? null : role.getRoleType().value(),
                role.getDataScopeType() == null ? null : role.getDataScopeType().value(),
                role.getStatus() == null ? null : role.getStatus().value());
    }

    public static Role toDomain(RoleDO dataObject) {
        return Role.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getCode(),
                dataObject.getName(),
                RoleType.from(dataObject.getRoleType()),
                RoleDataScopeType.from(dataObject.getDataScopeType()),
                RoleStatus.from(dataObject.getStatus()));
    }
}
