package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;

import java.util.List;
import java.util.Set;

public interface PermissionRepository {

    List<Menu> listMenus(Long tenantId);

    List<Menu> getUserMenuTree(Long tenantId, UserId userId);

    Set<String> getUserPermissionCodes(Long tenantId, UserId userId);

    Set<Long> getUserDepartmentIds(Long tenantId, UserId userId);

    Set<String> getUserScopeTypes(Long tenantId, UserId userId);

    boolean hasAllAccess(Long tenantId, UserId userId);
}
