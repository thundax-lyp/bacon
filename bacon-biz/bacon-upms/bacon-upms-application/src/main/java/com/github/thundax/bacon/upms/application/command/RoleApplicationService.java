package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.dto.RolePageQueryDTO;
import com.github.thundax.bacon.upms.api.dto.RolePageResultDTO;
import com.github.thundax.bacon.upms.api.enums.UpmsStatusEnum;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import java.util.List;
import java.util.Set;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleApplicationService {

    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;

    public RoleApplicationService(RoleRepository roleRepository, TenantRepository tenantRepository) {
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
    }

    public RoleDTO getRoleById(TenantId tenantId, RoleId roleId) {
        return toDto(roleRepository.findRoleById(tenantId, roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId.value())));
    }

    public RoleDTO getRoleById(String tenantId, String roleId) {
        return getRoleById(requireExistingTenantId(tenantId), RoleId.of(roleId));
    }

    public List<RoleDTO> getRolesByUserId(TenantId tenantId, UserId userId) {
        String tenantIdValue = tenantId.value();
        return roleRepository.findRolesByUserId(tenantId, userId).stream()
                .map(role -> toDto(role, tenantIdValue))
                .toList();
    }

    public List<RoleDTO> getRolesByUserId(String tenantId, String userId) {
        return getRolesByUserId(requireExistingTenantId(tenantId), UserId.of(userId));
    }

    public RolePageResultDTO pageRoles(RolePageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        String tenantIdValue = query.getTenantId().value();
        return new RolePageResultDTO(roleRepository.pageRoles(query.getTenantId(), query.getCode(), query.getName(),
                query.getRoleType(), query.getStatus(), pageNo, pageSize).stream()
                .map(role -> toDto(role, tenantIdValue))
                .toList(),
                roleRepository.countRoles(query.getTenantId(), query.getCode(), query.getName(), query.getRoleType(),
                        query.getStatus()),
                pageNo, pageSize);
    }

    public RoleDTO createRole(TenantId tenantId, String code, String name, String roleType, String dataScopeType) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateRequired(roleType, "roleType");
        validateRequired(dataScopeType, "dataScopeType");
        return toDto(roleRepository.save(new Role(null, tenantId, normalize(code), normalize(name), normalize(roleType),
                normalize(dataScopeType), UpmsStatusEnum.ENABLED.value())));
    }

    public RoleDTO updateRole(TenantId tenantId, String roleId, String code, String name, String roleType, String dataScopeType) {
        RoleId domainRoleId = RoleId.of(roleId);
        Role currentRole = roleRepository.findRoleById(tenantId, domainRoleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateRequired(roleType, "roleType");
        validateRequired(dataScopeType, "dataScopeType");
        return toDto(roleRepository.save(new Role(
                currentRole.getId(),
                tenantId,
                normalize(code),
                normalize(name),
                normalize(roleType),
                normalize(dataScopeType),
                currentRole.getStatus(),
                currentRole.getCreatedBy(),
                currentRole.getCreatedAt(),
                currentRole.getUpdatedBy(),
                currentRole.getUpdatedAt())));
    }

    public RoleDTO updateRoleStatus(TenantId tenantId, String roleId, String status) {
        validateRequired(status, "status");
        return toDto(roleRepository.updateStatus(tenantId, RoleId.of(roleId), normalize(status)));
    }

    public void deleteRole(TenantId tenantId, String roleId) {
        RoleId domainRoleId = RoleId.of(roleId);
        roleRepository.findRoleById(tenantId, domainRoleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        roleRepository.deleteRole(tenantId, domainRoleId);
    }

    public Set<Long> getAssignedMenus(TenantId tenantId, String roleId) {
        return roleRepository.getAssignedMenus(tenantId, RoleId.of(roleId));
    }

    public Set<Long> assignMenus(TenantId tenantId, String roleId, Set<Long> menuIds) {
        return roleRepository.assignMenus(tenantId, RoleId.of(roleId), menuIds);
    }

    public Set<String> getAssignedResources(TenantId tenantId, String roleId) {
        return roleRepository.getAssignedResources(tenantId, RoleId.of(roleId));
    }

    public Set<String> assignResources(TenantId tenantId, String roleId, Set<String> resourceCodes) {
        return roleRepository.assignResources(tenantId, RoleId.of(roleId), resourceCodes);
    }

    public String getAssignedDataScopeType(TenantId tenantId, String roleId) {
        return roleRepository.getAssignedDataScopeType(tenantId, RoleId.of(roleId));
    }

    public Set<DepartmentId> getAssignedDataScopeDepartments(TenantId tenantId, String roleId) {
        return roleRepository.getAssignedDataScopeDepartments(tenantId, RoleId.of(roleId));
    }

    public Set<DepartmentId> assignDataScope(TenantId tenantId, String roleId, String dataScopeType, Set<String> departmentIds) {
        validateRequired(dataScopeType, "dataScopeType");
        return roleRepository.assignDataScope(tenantId, RoleId.of(roleId), normalize(dataScopeType), toDepartmentIds(departmentIds));
    }

    private RoleDTO toDto(Role role) {
        return toDto(role, role.getTenantId().value());
    }

    private RoleDTO toDto(Role role, String tenantIdValue) {
        return new RoleDTO(role.getId() == null ? null : role.getId().value(), tenantIdValue, role.getCode(), role.getName(),
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

    private TenantId requireExistingTenantId(String tenantId) {
        validateRequired(tenantId, "tenantId");
        return tenantRepository.findTenantByTenantId(TenantId.of(tenantId))
                .map(Tenant::getId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
    }

    private Set<DepartmentId> toDepartmentIds(Set<String> departmentIds) {
        return departmentIds == null ? Set.of() : departmentIds.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(DepartmentId::of)
                .collect(java.util.stream.Collectors.toSet());
    }

}
