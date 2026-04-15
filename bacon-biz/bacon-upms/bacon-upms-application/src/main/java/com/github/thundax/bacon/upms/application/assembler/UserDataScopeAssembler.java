package com.github.thundax.bacon.upms.application.assembler;

import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserDataScopeAssembler {

    private UserDataScopeAssembler() {}

    public static UserDataScopeDTO toDto(boolean allAccess, Set<String> scopeTypes, Set<DepartmentId> departmentIds) {
        return new UserDataScopeDTO(
                allAccess,
                scopeTypes,
                departmentIds == null
                        ? Set.of()
                        : departmentIds.stream().map(DepartmentId::value).collect(Collectors.toSet()));
    }
}
