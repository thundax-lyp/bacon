package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;

import java.util.List;
import java.util.Set;

public interface PermissionRepository {

    List<Menu> listMenus(TenantId tenantId);

    List<Menu> getUserMenuTree(TenantId tenantId, UserId userId);

    Set<String> getUserPermissionCodes(TenantId tenantId, UserId userId);

    Set<DepartmentId> getUserDepartmentIds(TenantId tenantId, UserId userId);

    Set<String> getUserScopeTypes(TenantId tenantId, UserId userId);

    boolean hasAllAccess(TenantId tenantId, UserId userId);
}
