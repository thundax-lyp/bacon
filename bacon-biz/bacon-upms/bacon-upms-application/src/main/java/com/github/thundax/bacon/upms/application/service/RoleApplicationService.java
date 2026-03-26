package com.github.thundax.bacon.upms.application.service;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.dto.RolePageQueryDTO;
import com.github.thundax.bacon.upms.api.dto.RolePageResultDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import java.util.List;
import java.util.Set;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleApplicationService {

    private final RoleRepository roleRepository;

    public RoleApplicationService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public RoleDTO getRoleById(Long tenantId, Long roleId) {
        return toDto(roleRepository.findRoleById(tenantId, roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId)));
    }

    public List<RoleDTO> getRolesByUserId(Long tenantId, Long userId) {
        return roleRepository.findRolesByUserId(tenantId, userId).stream()
                .map(this::toDto)
                .toList();
    }

    public RolePageResultDTO pageRoles(RolePageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        return new RolePageResultDTO(roleRepository.pageRoles(query.getTenantId(), query.getCode(), query.getName(),
                query.getRoleType(), query.getStatus(), pageNo, pageSize).stream().map(this::toDto).toList(),
                roleRepository.countRoles(query.getTenantId(), query.getCode(), query.getName(), query.getRoleType(),
                        query.getStatus()),
                pageNo, pageSize);
    }

    public RoleDTO createRole(Long tenantId, String code, String name, String roleType, String dataScopeType) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateRequired(roleType, "roleType");
        validateRequired(dataScopeType, "dataScopeType");
        return toDto(roleRepository.save(new Role(null, tenantId, normalize(code), normalize(name), normalize(roleType),
                normalize(dataScopeType), "ENABLED")));
    }

    public RoleDTO updateRole(Long tenantId, Long roleId, String code, String name, String roleType, String dataScopeType) {
        Role currentRole = roleRepository.findRoleById(tenantId, roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateRequired(roleType, "roleType");
        validateRequired(dataScopeType, "dataScopeType");
        return toDto(roleRepository.save(new Role(currentRole.getId(), currentRole.getCreatedBy(), currentRole.getCreatedAt(),
                currentRole.getUpdatedBy(), currentRole.getUpdatedAt(), tenantId, normalize(code), normalize(name),
                normalize(roleType), normalize(dataScopeType), currentRole.getStatus())));
    }

    public RoleDTO updateRoleStatus(Long tenantId, Long roleId, String status) {
        validateRequired(status, "status");
        return toDto(roleRepository.updateStatus(tenantId, roleId, normalize(status)));
    }

    public void deleteRole(Long tenantId, Long roleId) {
        roleRepository.findRoleById(tenantId, roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        roleRepository.deleteRole(tenantId, roleId);
    }

    public Set<Long> getAssignedMenus(Long tenantId, Long roleId) {
        return roleRepository.getAssignedMenus(tenantId, roleId);
    }

    public Set<Long> assignMenus(Long tenantId, Long roleId, Set<Long> menuIds) {
        return roleRepository.assignMenus(tenantId, roleId, menuIds);
    }

    public Set<String> getAssignedResources(Long tenantId, Long roleId) {
        return roleRepository.getAssignedResources(tenantId, roleId);
    }

    public Set<String> assignResources(Long tenantId, Long roleId, Set<String> resourceCodes) {
        return roleRepository.assignResources(tenantId, roleId, resourceCodes);
    }

    public String getAssignedDataScopeType(Long tenantId, Long roleId) {
        return roleRepository.getAssignedDataScopeType(tenantId, roleId);
    }

    public Set<Long> getAssignedDataScopeDepartments(Long tenantId, Long roleId) {
        return roleRepository.getAssignedDataScopeDepartments(tenantId, roleId);
    }

    public Set<Long> assignDataScope(Long tenantId, Long roleId, String dataScopeType, Set<Long> departmentIds) {
        validateRequired(dataScopeType, "dataScopeType");
        return roleRepository.assignDataScope(tenantId, roleId, normalize(dataScopeType), departmentIds);
    }

    private RoleDTO toDto(Role role) {
        return new RoleDTO(role.getId(), role.getTenantId(), role.getCode(), role.getName(),
                role.getRoleType(), role.getDataScopeType(), role.getStatus());
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

}
