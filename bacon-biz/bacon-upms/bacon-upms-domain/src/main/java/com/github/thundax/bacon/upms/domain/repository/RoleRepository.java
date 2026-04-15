package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository {

    Optional<Role> findRoleById(RoleId roleId);

    List<Role> findRolesByUserId(UserId userId);

    List<Role> pageRoles(String code, String name, RoleType roleType, RoleStatus status, int pageNo, int pageSize);

    long countRoles(String code, String name, RoleType roleType, RoleStatus status);

    Role insert(Role role);

    Role update(Role role);

    Role updateStatus(RoleId roleId, RoleStatus status);

    void deleteRole(RoleId roleId);

    Set<MenuId> getAssignedMenus(RoleId roleId);

    Set<MenuId> assignMenus(RoleId roleId, Set<MenuId> menuIds);

    Set<String> getAssignedResources(RoleId roleId);

    Set<String> assignResources(RoleId roleId, Set<String> resourceCodes);

    RoleDataScopeType getAssignedDataScopeType(RoleId roleId);

    Set<DepartmentId> getAssignedDataScopeDepartments(RoleId roleId);

    Set<DepartmentId> assignDataScope(RoleId roleId, RoleDataScopeType dataScopeType, Set<DepartmentId> departmentIds);
}
