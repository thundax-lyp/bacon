package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.domain.DepartmentId;
import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.domain.MenuId;
import com.github.thundax.bacon.common.id.domain.RoleId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.dto.RolePageQueryDTO;
import com.github.thundax.bacon.upms.api.dto.RolePageResultDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.entity.Tenant;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return getRoleById(requireExistingTenantId(tenantId), toRoleId(roleId));
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

    @Transactional
    public RoleDTO createRole(TenantId tenantId, String code, String name, String roleType, String dataScopeType) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateRequired(roleType, "roleType");
        validateRequired(dataScopeType, "dataScopeType");
        return toDto(roleRepository.save(new Role(null, tenantId, normalize(code), normalize(name), toRoleType(roleType),
                toRoleDataScopeType(dataScopeType), RoleStatus.ENABLED)));
    }

    @Transactional
    public RoleDTO updateRole(TenantId tenantId, String roleId, String code, String name, String roleType, String dataScopeType) {
        RoleId domainRoleId = toRoleId(roleId);
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
                toRoleType(roleType),
                toRoleDataScopeType(dataScopeType),
                currentRole.getStatus(),
                currentRole.getCreatedBy(),
                currentRole.getCreatedAt(),
                currentRole.getUpdatedBy(),
                currentRole.getUpdatedAt())));
    }

    @Transactional
    public RoleDTO updateRoleStatus(TenantId tenantId, String roleId, String status) {
        validateRequired(status, "status");
        return toDto(roleRepository.updateStatus(tenantId, toRoleId(roleId), toRoleStatus(status)));
    }

    @Transactional
    public void deleteRole(TenantId tenantId, String roleId) {
        RoleId domainRoleId = toRoleId(roleId);
        roleRepository.findRoleById(tenantId, domainRoleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        roleRepository.deleteRole(tenantId, domainRoleId);
    }

    public Set<String> getAssignedMenus(TenantId tenantId, String roleId) {
        return roleRepository.getAssignedMenus(tenantId, toRoleId(roleId)).stream()
                .map(menuId -> String.valueOf(menuId.value()))
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    @Transactional
    public Set<String> assignMenus(TenantId tenantId, String roleId, Set<String> menuIds) {
        return roleRepository.assignMenus(tenantId, toRoleId(roleId), toMenuIds(menuIds)).stream()
                .map(menuId -> String.valueOf(menuId.value()))
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    public Set<String> getAssignedResources(TenantId tenantId, String roleId) {
        return roleRepository.getAssignedResources(tenantId, toRoleId(roleId));
    }

    @Transactional
    public Set<String> assignResources(TenantId tenantId, String roleId, Set<String> resourceCodes) {
        return roleRepository.assignResources(tenantId, toRoleId(roleId), resourceCodes);
    }

    public String getAssignedDataScopeType(TenantId tenantId, String roleId) {
        return roleRepository.getAssignedDataScopeType(tenantId, toRoleId(roleId));
    }

    public Set<DepartmentId> getAssignedDataScopeDepartments(TenantId tenantId, String roleId) {
        return roleRepository.getAssignedDataScopeDepartments(tenantId, toRoleId(roleId));
    }

    @Transactional
    public Set<DepartmentId> assignDataScope(TenantId tenantId, String roleId, String dataScopeType, Set<String> departmentIds) {
        validateRequired(dataScopeType, "dataScopeType");
        return roleRepository.assignDataScope(tenantId, toRoleId(roleId), toRoleDataScopeType(dataScopeType),
                toDepartmentIds(departmentIds));
    }

    private RoleDTO toDto(Role role) {
        return toDto(role, role.getTenantId().value());
    }

    private RoleDTO toDto(Role role, String tenantIdValue) {
        return new RoleDTO(role.getId() == null ? null : String.valueOf(role.getId().value()), tenantIdValue, role.getCode(), role.getName(),
                role.getRoleType() == null ? null : role.getRoleType().value(),
                role.getDataScopeType() == null ? null : role.getDataScopeType().value(),
                role.getStatus() == null ? null : role.getStatus().value());
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private RoleType toRoleType(String roleType) {
        validateRequired(roleType, "roleType");
        return RoleType.fromValue(normalize(roleType).toUpperCase(Locale.ROOT));
    }

    private RoleDataScopeType toRoleDataScopeType(String dataScopeType) {
        validateRequired(dataScopeType, "dataScopeType");
        return RoleDataScopeType.fromValue(normalize(dataScopeType).toUpperCase(Locale.ROOT));
    }

    private RoleStatus toRoleStatus(String status) {
        validateRequired(status, "status");
        return RoleStatus.fromValue(normalize(status).toUpperCase(Locale.ROOT));
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
                .map(Long::parseLong)
                .map(DepartmentId::of)
                .collect(java.util.stream.Collectors.toSet());
    }

    private Set<MenuId> toMenuIds(Set<String> menuIds) {
        return menuIds == null ? Set.of() : menuIds.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(Long::parseLong)
                .map(MenuId::of)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private RoleId toRoleId(String roleId) {
        validateRequired(roleId, "roleId");
        return RoleId.of(Long.parseLong(roleId.trim()));
    }

}
