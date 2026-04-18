package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.Set;

public interface RoleDataScopeRepository {

    RoleDataScopeType findDataScopeType(RoleId roleId);

    Set<DepartmentId> findDataScopeDepartmentIds(RoleId roleId);

    Set<DepartmentId> updateDataScope(RoleId roleId, RoleDataScopeType dataScopeType, Set<DepartmentId> departmentIds);
}
