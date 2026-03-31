package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository {

    Optional<Role> findRoleById(TenantId tenantId, Long roleId);

    List<Role> findRolesByUserId(TenantId tenantId, UserId userId);

    List<Role> pageRoles(TenantId tenantId, String code, String name, String roleType, String status, int pageNo, int pageSize);

    long countRoles(TenantId tenantId, String code, String name, String roleType, String status);

    Role save(Role role);

    Role updateStatus(TenantId tenantId, Long roleId, String status);

    void deleteRole(TenantId tenantId, Long roleId);

    Set<Long> getAssignedMenus(TenantId tenantId, Long roleId);

    Set<Long> assignMenus(TenantId tenantId, Long roleId, Set<Long> menuIds);

    Set<String> getAssignedResources(TenantId tenantId, Long roleId);

    Set<String> assignResources(TenantId tenantId, Long roleId, Set<String> resourceCodes);

    String getAssignedDataScopeType(TenantId tenantId, Long roleId);

    Set<DepartmentId> getAssignedDataScopeDepartments(TenantId tenantId, Long roleId);

    Set<DepartmentId> assignDataScope(TenantId tenantId, Long roleId, String dataScopeType, Set<DepartmentId> departmentIds);
}
