package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.util.List;
import java.util.Set;

public interface PermissionRepository {

    List<Menu> listMenus();

    List<Menu> getUserMenuTree(UserId userId);

    Set<String> getUserPermissionCodes(UserId userId);

    Set<DepartmentId> getUserDepartmentIds(UserId userId);

    Set<String> getUserScopeTypes(UserId userId);

    boolean hasAllAccess(UserId userId);
}
