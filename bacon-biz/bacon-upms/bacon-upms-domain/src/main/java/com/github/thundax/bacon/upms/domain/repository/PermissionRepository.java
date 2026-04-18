package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.util.List;
import java.util.Set;

public interface PermissionRepository {

    List<Menu> listMenus();

    List<Menu> listUserMenuTree(UserId userId);

    Set<String> findUserPermissionCodes(UserId userId);

    Set<DepartmentId> findUserDepartmentIds(UserId userId);

    Set<String> findUserScopeTypes(UserId userId);

    boolean existsUserAllAccess(UserId userId);
}
