package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.entity.Menu;
import java.util.List;
import java.util.Set;

public interface PermissionRepository {

    List<Menu> getUserMenuTree(Long tenantId, Long userId);

    Set<String> getUserPermissionCodes(Long tenantId, Long userId);

    Set<Long> getUserDepartmentIds(Long tenantId, Long userId);

    Set<String> getUserScopeTypes(Long tenantId, Long userId);

    boolean hasAllAccess(Long tenantId, Long userId);
}
