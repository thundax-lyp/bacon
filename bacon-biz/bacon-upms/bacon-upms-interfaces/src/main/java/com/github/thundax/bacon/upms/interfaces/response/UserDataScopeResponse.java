package com.github.thundax.bacon.upms.interfaces.response;

import com.github.thundax.bacon.upms.api.dto.UserDataScopeDTO;
import java.util.Set;

/**
 * 用户数据范围响应对象。
 */
public record UserDataScopeResponse(
        /** 是否拥有全部数据权限。 */
        boolean allAccess,
        /** 数据范围类型集合。 */
        Set<String> scopeTypes,
        /** 可访问部门主键集合。 */
        Set<Long> departmentIds) {

    public static UserDataScopeResponse from(UserDataScopeDTO dto) {
        return new UserDataScopeResponse(dto.isAllAccess(), dto.getScopeTypes(), dto.getDepartmentIds());
    }
}
