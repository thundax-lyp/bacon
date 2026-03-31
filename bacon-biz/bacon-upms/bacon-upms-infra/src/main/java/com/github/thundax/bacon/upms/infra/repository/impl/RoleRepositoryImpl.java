package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(UpmsRepositorySupport.class)
public class RoleRepositoryImpl implements RoleRepository {

    private final UpmsRepositorySupport support;

    public RoleRepositoryImpl(UpmsRepositorySupport support) {
        this.support = support;
    }

    @Override
    public Optional<Role> findRoleById(Long tenantId, Long roleId) {
        return support.findRoleById(tenantId, roleId);
    }

    @Override
    public List<Role> findRolesByUserId(Long tenantId, Long userId) {
        return support.findRolesByUserId(tenantId, userId);
    }

    @Override
    public List<Role> pageRoles(Long tenantId, String code, String name, String roleType, String status, int pageNo, int pageSize) {
        return support.listRoles(tenantId, code, name, roleType, status, pageNo, pageSize);
    }

    @Override
    public long countRoles(Long tenantId, String code, String name, String roleType, String status) {
        return support.countRoles(tenantId, code, name, roleType, status);
    }

    @Override
    public Role save(Role role) {
        return support.saveRole(role);
    }

    @Override
    public Role updateStatus(Long tenantId, Long roleId, String status) {
        Role currentRole = findRoleById(tenantId, roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return support.saveRole(new Role(
                currentRole.getId(),
                currentRole.getTenantId(),
                currentRole.getCode(),
                currentRole.getName(),
                currentRole.getRoleType(),
                currentRole.getDataScopeType(),
                status,
                currentRole.getCreatedBy(),
                currentRole.getCreatedAt(),
                currentRole.getUpdatedBy(),
                currentRole.getUpdatedAt()));
    }

    @Override
    public void deleteRole(Long tenantId, Long roleId) {
        support.deleteRole(tenantId, roleId);
    }

    @Override
    public Set<Long> getAssignedMenus(Long tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return support.getAssignedMenuIds(tenantId, roleId);
    }

    @Override
    public Set<Long> assignMenus(Long tenantId, Long roleId, Set<Long> menuIds) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Set<Long> safeMenuIds = menuIds == null ? Set.of() : Set.copyOf(menuIds);
        support.replaceRoleMenus(tenantId, roleId, safeMenuIds);
        return safeMenuIds;
    }

    @Override
    public Set<String> getAssignedResources(Long tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return support.getAssignedResourceCodes(tenantId, roleId);
    }

    @Override
    public Set<String> assignResources(Long tenantId, Long roleId, Set<String> resourceCodes) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Set<String> safeResourceCodes = resourceCodes == null ? Set.of() : Set.copyOf(resourceCodes);
        support.replaceRoleResources(tenantId, roleId, safeResourceCodes);
        return safeResourceCodes;
    }

    @Override
    public String getAssignedDataScopeType(Long tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return support.getAssignedDataScopeType(tenantId, roleId);
    }

    @Override
    public Set<Long> getAssignedDataScopeDepartments(Long tenantId, Long roleId) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return support.getAssignedDataScopeDepartments(tenantId, roleId);
    }

    @Override
    public Set<Long> assignDataScope(Long tenantId, Long roleId, String dataScopeType, Set<Long> departmentIds) {
        findRoleById(tenantId, roleId).orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Set<Long> safeDepartmentIds = departmentIds == null ? Set.of() : Set.copyOf(departmentIds);
        support.replaceRoleDataScope(tenantId, roleId, dataScopeType, safeDepartmentIds);
        Role currentRole = findRoleById(tenantId, roleId).orElseThrow();
        support.saveRole(new Role(currentRole.getId(), currentRole.getTenantId(), currentRole.getCode(), currentRole.getName(),
                currentRole.getRoleType(), dataScopeType, currentRole.getStatus(), currentRole.getCreatedBy(),
                currentRole.getCreatedAt(), currentRole.getUpdatedBy(), currentRole.getUpdatedAt()));
        return safeDepartmentIds;
    }

    void bindUserRoles(Long tenantId, Long userId, List<Role> assignedRoles) {
        support.replaceUserRoles(tenantId, userId, assignedRoles.stream().map(Role::getId).toList());
    }

    void clearUserRoles(Long tenantId, Long userId) {
        support.deleteUserRolesByUser(tenantId, userId);
    }

    void removeMenuFromAssignments(Long tenantId, Long menuId) {
        support.removeMenuFromAssignments(tenantId, menuId);
    }
}
