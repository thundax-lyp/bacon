package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
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

    public RoleDTO getRoleById(TenantId tenantId, Long roleId) {
        return toDto(roleRepository.findRoleById(tenantId, roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId)));
    }

    public RoleDTO getRoleById(String tenantNo, Long roleId) {
        return getRoleById(resolveTenantIdByTenantNo(tenantNo), roleId);
    }

    public List<RoleDTO> getRolesByUserId(TenantId tenantId, UserId userId) {
        String tenantNo = resolveTenantNoByTenantId(tenantId);
        return roleRepository.findRolesByUserId(tenantId, userId).stream()
                .map(role -> toDto(role, tenantNo))
                .toList();
    }

    public List<RoleDTO> getRolesByUserId(String tenantNo, String userId) {
        return getRolesByUserId(resolveTenantIdByTenantNo(tenantNo), UserId.of(userId));
    }

    public RolePageResultDTO pageRoles(RolePageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        String tenantNo = resolveTenantNoByTenantId(query.getTenantId());
        return new RolePageResultDTO(roleRepository.pageRoles(query.getTenantId(), query.getCode(), query.getName(),
                query.getRoleType(), query.getStatus(), pageNo, pageSize).stream()
                .map(role -> toDto(role, tenantNo))
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

    public RoleDTO updateRole(TenantId tenantId, Long roleId, String code, String name, String roleType, String dataScopeType) {
        Role currentRole = roleRepository.findRoleById(tenantId, roleId)
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

    public RoleDTO updateRoleStatus(TenantId tenantId, Long roleId, String status) {
        validateRequired(status, "status");
        return toDto(roleRepository.updateStatus(tenantId, roleId, normalize(status)));
    }

    public void deleteRole(TenantId tenantId, Long roleId) {
        roleRepository.findRoleById(tenantId, roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        roleRepository.deleteRole(tenantId, roleId);
    }

    public Set<Long> getAssignedMenus(TenantId tenantId, Long roleId) {
        return roleRepository.getAssignedMenus(tenantId, roleId);
    }

    public Set<Long> assignMenus(TenantId tenantId, Long roleId, Set<Long> menuIds) {
        return roleRepository.assignMenus(tenantId, roleId, menuIds);
    }

    public Set<String> getAssignedResources(TenantId tenantId, Long roleId) {
        return roleRepository.getAssignedResources(tenantId, roleId);
    }

    public Set<String> assignResources(TenantId tenantId, Long roleId, Set<String> resourceCodes) {
        return roleRepository.assignResources(tenantId, roleId, resourceCodes);
    }

    public String getAssignedDataScopeType(TenantId tenantId, Long roleId) {
        return roleRepository.getAssignedDataScopeType(tenantId, roleId);
    }

    public Set<Long> getAssignedDataScopeDepartments(TenantId tenantId, Long roleId) {
        return roleRepository.getAssignedDataScopeDepartments(tenantId, roleId);
    }

    public Set<Long> assignDataScope(TenantId tenantId, Long roleId, String dataScopeType, Set<Long> departmentIds) {
        validateRequired(dataScopeType, "dataScopeType");
        return roleRepository.assignDataScope(tenantId, roleId, normalize(dataScopeType), departmentIds);
    }

    private RoleDTO toDto(Role role) {
        return toDto(role, resolveTenantNoByTenantId(role.getTenantId()));
    }

    private RoleDTO toDto(Role role, String tenantNo) {
        return new RoleDTO(role.getId(), tenantNo, role.getCode(), role.getName(),
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

    private TenantId resolveTenantIdByTenantNo(String tenantNo) {
        validateRequired(tenantNo, "tenantNo");
        return tenantRepository.findTenantByTenantId(TenantId.of(tenantNo))
                .map(Tenant::getId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantNo));
    }

    private String resolveTenantNoByTenantId(TenantId tenantId) {
        return tenantRepository.findTenantById(tenantId)
                .map(tenant -> tenant.getId().value())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId.value()));
    }

}
