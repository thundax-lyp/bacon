package com.github.thundax.bacon.upms.interfaces.response;

import java.util.Set;

/**
 * 角色数据范围响应对象。
 */
public record RoleDataScopeResponse(
        /** 数据范围类型。 */
        String dataScopeType,
        /** 已分配部门主键集合。 */
        Set<Long> departmentIds) {}
