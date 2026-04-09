package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository {

    Optional<Role> findRoleById(TenantId tenantId, RoleId roleId);

    List<Role> findRolesByUserId(TenantId tenantId, UserId userId);

    List<Role> pageRoles(
            TenantId tenantId, String code, String name, String roleType, String status, int pageNo, int pageSize);

    long countRoles(TenantId tenantId, String code, String name, String roleType, String status);

    Role save(Role role);

    Role updateStatus(TenantId tenantId, RoleId roleId, RoleStatus status);

    void deleteRole(TenantId tenantId, RoleId roleId);

    Set<MenuId> getAssignedMenus(TenantId tenantId, RoleId roleId);

    Set<MenuId> assignMenus(TenantId tenantId, RoleId roleId, Set<MenuId> menuIds);

    Set<String> getAssignedResources(TenantId tenantId, RoleId roleId);

    Set<String> assignResources(TenantId tenantId, RoleId roleId, Set<String> resourceCodes);

    String getAssignedDataScopeType(TenantId tenantId, RoleId roleId);

    Set<DepartmentId> getAssignedDataScopeDepartments(TenantId tenantId, RoleId roleId);

    Set<DepartmentId> assignDataScope(
            TenantId tenantId, RoleId roleId, RoleDataScopeType dataScopeType, Set<DepartmentId> departmentIds);
}
