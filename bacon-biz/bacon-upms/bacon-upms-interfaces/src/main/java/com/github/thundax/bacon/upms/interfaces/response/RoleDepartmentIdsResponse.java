package com.github.thundax.bacon.upms.interfaces.response;

import java.util.Set;

/**
 * 角色数据范围部门集合响应对象。
 */
public record RoleDepartmentIdsResponse(Set<Long> departmentIds) {}
