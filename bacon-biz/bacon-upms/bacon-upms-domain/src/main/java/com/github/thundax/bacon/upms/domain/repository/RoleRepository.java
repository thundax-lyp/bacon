package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository {

    Optional<Role> findRoleById(TenantId tenantId, RoleId roleId);

    List<Role> findRolesByUserId(TenantId tenantId, UserId userId);

    List<Role> pageRoles(TenantId tenantId, String code, String name, String roleType, String status, int pageNo, int pageSize);

    long countRoles(TenantId tenantId, String code, String name, String roleType, String status);

    Role save(Role role);

    Role updateStatus(TenantId tenantId, RoleId roleId, String status);

    void deleteRole(TenantId tenantId, RoleId roleId);

    Set<Long> getAssignedMenus(TenantId tenantId, RoleId roleId);

    Set<Long> assignMenus(TenantId tenantId, RoleId roleId, Set<Long> menuIds);

    Set<String> getAssignedResources(TenantId tenantId, RoleId roleId);

    Set<String> assignResources(TenantId tenantId, RoleId roleId, Set<String> resourceCodes);

    String getAssignedDataScopeType(TenantId tenantId, RoleId roleId);

    Set<DepartmentId> getAssignedDataScopeDepartments(TenantId tenantId, RoleId roleId);

    Set<DepartmentId> assignDataScope(TenantId tenantId, RoleId roleId, String dataScopeType, Set<DepartmentId> departmentIds);
}
