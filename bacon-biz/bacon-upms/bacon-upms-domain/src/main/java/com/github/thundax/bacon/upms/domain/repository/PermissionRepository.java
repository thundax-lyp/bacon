package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import java.util.List;
import java.util.Set;

public interface PermissionRepository {

    List<Menu> list();

    List<Menu> listMenuTreeByUserId(UserId userId);

    Set<String> findPermissionCodesByUserId(UserId userId);

    Set<DepartmentId> findDepartmentIdsByUserId(UserId userId);

    Set<String> findScopeTypesByUserId(UserId userId);

    boolean existsAllAccessByUserId(UserId userId);
}
