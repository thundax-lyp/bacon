package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.Set;

public interface RoleMenuRepository {

    Set<MenuId> findMenuIds(RoleId roleId);

    Set<MenuId> updateMenuIds(RoleId roleId, Set<MenuId> menuIds);
}
