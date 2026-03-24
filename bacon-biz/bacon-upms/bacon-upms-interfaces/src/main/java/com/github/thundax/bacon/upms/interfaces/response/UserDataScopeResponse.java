package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import java.util.Set;

public record UserDataScopeResponse(boolean allAccess, Set<String> scopeTypes, Set<Long> departmentIds) {

    public static UserDataScopeResponse from(UserDataScopeDTO dto) {
        return new UserDataScopeResponse(dto.isAllAccess(), dto.getScopeTypes(), dto.getDepartmentIds());
    }
}
