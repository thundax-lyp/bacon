package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.PageResultDTO;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.api.dto.RolePageQueryDTO;
import com.github.thundax.bacon.upms.application.assembler.RoleAssembler;
import com.github.thundax.bacon.upms.application.codec.DepartmentIdCodec;
import com.github.thundax.bacon.upms.application.codec.MenuIdCodec;
import com.github.thundax.bacon.upms.application.codec.RoleIdCodec;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleApplicationService {

    private static final String ROLE_ID_BIZ_TAG = "role-id";

    private final RoleRepository roleRepository;
    private final IdGenerator idGenerator;

    public RoleApplicationService(RoleRepository roleRepository, IdGenerator idGenerator) {
        this.roleRepository = roleRepository;
        this.idGenerator = idGenerator;
    }

    public RoleDTO getRoleById(RoleId roleId) {
        return toDto(roleRepository
                .findRoleById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId.value())));
    }

    public List<RoleDTO> getRolesByUserId(UserId userId) {
        return roleRepository.findRolesByUserId(userId).stream()
                .map(RoleAssembler::toDto)
                .toList();
    }

    public PageResultDTO<RoleDTO> pageRoles(RolePageQueryDTO query) {
        int pageNo = PageParamNormalizer.normalizePageNo(query.getPageNo());
        int pageSize = PageParamNormalizer.normalizePageSize(query.getPageSize());
        return new PageResultDTO<>(
                roleRepository
                        .pageRoles(
                                query.getCode(),
                                query.getName(),
                                query.getRoleType(),
                                query.getStatus(),
                                pageNo,
                                pageSize)
                        .stream()
                        .map(this::toDto)
                        .toList(),
                roleRepository.countRoles(query.getCode(), query.getName(), query.getRoleType(), query.getStatus()),
                pageNo,
                pageSize);
    }

    @Transactional
    public RoleDTO createRole(String code, String name, String roleType, String dataScopeType) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateRequired(roleType, "roleType");
        validateRequired(dataScopeType, "dataScopeType");
        return toDto(roleRepository.save(Role.create(
                RoleIdCodec.toDomain(idGenerator.nextId(ROLE_ID_BIZ_TAG)),
                normalize(code),
                normalize(name),
                toRoleType(roleType),
                toRoleDataScopeType(dataScopeType),
                RoleStatus.ENABLED)));
    }

    @Transactional
    public RoleDTO updateRole(String roleId, String code, String name, String roleType, String dataScopeType) {
        RoleId domainRoleId = toRoleId(roleId);
        Role currentRole = roleRepository
                .findRoleById(domainRoleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        validateRequired(code, "code");
        validateRequired(name, "name");
        validateRequired(roleType, "roleType");
        validateRequired(dataScopeType, "dataScopeType");
        return toDto(roleRepository.save(currentRole.update(
                normalize(code),
                normalize(name),
                toRoleType(roleType),
                toRoleDataScopeType(dataScopeType),
                currentRole.getStatus())));
    }

    @Transactional
    public RoleDTO updateRoleStatus(String roleId, String status) {
        validateRequired(status, "status");
        return toDto(roleRepository.updateStatus(toRoleId(roleId), toRoleStatus(status)));
    }

    @Transactional
    public void deleteRole(String roleId) {
        RoleId domainRoleId = toRoleId(roleId);
        roleRepository
                .findRoleById(domainRoleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        roleRepository.deleteRole(domainRoleId);
    }

    public Set<String> getAssignedMenus(String roleId) {
        return roleRepository.getAssignedMenus(toRoleId(roleId)).stream()
                .map(MenuIdCodec::toValue)
                .map(String::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Transactional
    public Set<String> assignMenus(String roleId, Set<String> menuIds) {
        return roleRepository.assignMenus(toRoleId(roleId), toMenuIds(menuIds)).stream()
                .map(MenuIdCodec::toValue)
                .map(String::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> getAssignedResources(String roleId) {
        return roleRepository.getAssignedResources(toRoleId(roleId));
    }

    @Transactional
    public Set<String> assignResources(String roleId, Set<String> resourceCodes) {
        return roleRepository.assignResources(toRoleId(roleId), resourceCodes);
    }

    public String getAssignedDataScopeType(String roleId) {
        return roleRepository.getAssignedDataScopeType(toRoleId(roleId));
    }

    public Set<DepartmentId> getAssignedDataScopeDepartments(String roleId) {
        return roleRepository.getAssignedDataScopeDepartments(toRoleId(roleId));
    }

    @Transactional
    public Set<DepartmentId> assignDataScope(String roleId, String dataScopeType, Set<String> departmentIds) {
        validateRequired(dataScopeType, "dataScopeType");
        return roleRepository.assignDataScope(
                toRoleId(roleId), toRoleDataScopeType(dataScopeType), toDepartmentIds(departmentIds));
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
        return RoleType.from(normalize(roleType).toUpperCase(Locale.ROOT));
    }

    private RoleDataScopeType toRoleDataScopeType(String dataScopeType) {
        validateRequired(dataScopeType, "dataScopeType");
        return RoleDataScopeType.from(normalize(dataScopeType).toUpperCase(Locale.ROOT));
    }

    private RoleStatus toRoleStatus(String status) {
        validateRequired(status, "status");
        return RoleStatus.from(normalize(status).toUpperCase(Locale.ROOT));
    }

    private RoleDTO toDto(Role role) {
        return RoleAssembler.toDto(role);
    }

    private Set<DepartmentId> toDepartmentIds(Set<String> departmentIds) {
        return departmentIds == null
                ? Set.of()
                : departmentIds.stream()
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .map(Long::parseLong)
                        .map(DepartmentIdCodec::toDomain)
                        .collect(Collectors.toSet());
    }

    private Set<MenuId> toMenuIds(Set<String> menuIds) {
        return menuIds == null
                ? Set.of()
                : menuIds.stream()
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .map(Long::parseLong)
                        .map(MenuIdCodec::toDomain)
                        .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private RoleId toRoleId(String roleId) {
        validateRequired(roleId, "roleId");
        return RoleIdCodec.toDomain(Long.parseLong(roleId.trim()));
    }
}
