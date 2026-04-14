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
        return RoleAssembler.toDto(roleRepository
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
                        .map(RoleAssembler::toDto)
                        .toList(),
                roleRepository.countRoles(query.getCode(), query.getName(), query.getRoleType(), query.getStatus()),
                pageNo,
                pageSize);
    }

    @Transactional
    public RoleDTO createRole(String code, String name, RoleType roleType, RoleDataScopeType dataScopeType) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        return RoleAssembler.toDto(roleRepository.insert(Role.create(
                RoleIdCodec.toDomain(idGenerator.nextId(ROLE_ID_BIZ_TAG)),
                normalize(code),
                normalize(name),
                roleType,
                dataScopeType,
                RoleStatus.ENABLED)));
    }

    @Transactional
    public RoleDTO updateRole(RoleId roleId, String code, String name, RoleType roleType, RoleDataScopeType dataScopeType) {
        Role currentRole = roleRepository
                .findRoleById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        validateRequired(code, "code");
        validateRequired(name, "name");
        return RoleAssembler.toDto(roleRepository.update(currentRole.update(
                normalize(code),
                normalize(name),
                roleType,
                dataScopeType,
                currentRole.getStatus())));
    }

    @Transactional
    public RoleDTO updateRoleStatus(RoleId roleId, RoleStatus status) {
        return RoleAssembler.toDto(roleRepository.updateStatus(roleId, status));
    }

    @Transactional
    public void deleteRole(RoleId roleId) {
        roleRepository
                .findRoleById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        roleRepository.deleteRole(roleId);
    }

    public Set<String> getAssignedMenus(RoleId roleId) {
        return roleRepository.getAssignedMenus(roleId).stream()
                .map(MenuIdCodec::toValue)
                .map(String::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Transactional
    public Set<String> assignMenus(RoleId roleId, Set<MenuId> menuIds) {
        return roleRepository.assignMenus(roleId, menuIds).stream()
                .map(MenuIdCodec::toValue)
                .map(String::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> getAssignedResources(RoleId roleId) {
        return roleRepository.getAssignedResources(roleId);
    }

    @Transactional
    public Set<String> assignResources(RoleId roleId, Set<String> resourceCodes) {
        return roleRepository.assignResources(roleId, resourceCodes);
    }

    public RoleDataScopeType getAssignedDataScopeType(RoleId roleId) {
        return roleRepository.getAssignedDataScopeType(roleId);
    }

    public Set<DepartmentId> getAssignedDataScopeDepartments(RoleId roleId) {
        return roleRepository.getAssignedDataScopeDepartments(roleId);
    }

    @Transactional
    public Set<DepartmentId> assignDataScope(RoleId roleId, RoleDataScopeType dataScopeType, Set<DepartmentId> departmentIds) {
        return roleRepository.assignDataScope(roleId, dataScopeType, departmentIds);
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
