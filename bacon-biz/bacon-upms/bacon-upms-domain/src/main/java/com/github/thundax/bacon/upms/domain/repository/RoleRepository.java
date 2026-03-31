package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository {

    Optional<Role> findRoleById(Long tenantId, Long roleId);

    List<Role> findRolesByUserId(Long tenantId, UserId userId);

    List<Role> pageRoles(Long tenantId, String code, String name, String roleType, String status, int pageNo, int pageSize);

    long countRoles(Long tenantId, String code, String name, String roleType, String status);

    Role save(Role role);

    Role updateStatus(Long tenantId, Long roleId, String status);

    void deleteRole(Long tenantId, Long roleId);

    Set<Long> getAssignedMenus(Long tenantId, Long roleId);

    Set<Long> assignMenus(Long tenantId, Long roleId, Set<Long> menuIds);

    Set<String> getAssignedResources(Long tenantId, Long roleId);

    Set<String> assignResources(Long tenantId, Long roleId, Set<String> resourceCodes);

    String getAssignedDataScopeType(Long tenantId, Long roleId);

    Set<Long> getAssignedDataScopeDepartments(Long tenantId, Long roleId);

    Set<Long> assignDataScope(Long tenantId, Long roleId, String dataScopeType, Set<Long> departmentIds);
}
