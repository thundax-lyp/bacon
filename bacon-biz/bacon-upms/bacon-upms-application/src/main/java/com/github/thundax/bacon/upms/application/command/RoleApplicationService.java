package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.util.PageParamNormalizer;
import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.api.dto.PageResultDTO;
import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.application.assembler.RoleAssembler;
import com.github.thundax.bacon.upms.application.codec.MenuIdCodec;
import com.github.thundax.bacon.upms.application.codec.ResourceCodeCodec;
import com.github.thundax.bacon.upms.application.codec.RoleCodeCodec;
import com.github.thundax.bacon.upms.application.codec.RoleIdCodec;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleDataScopeAssignment;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import java.util.LinkedHashSet;
import java.util.List;
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
                .findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleId.value())));
    }

    public List<RoleDTO> getRolesByUserId(UserId userId) {
        return roleRepository.findByUserId(userId).stream()
                .map(RoleAssembler::toDto)
                .toList();
    }

    public PageResultDTO<RoleDTO> page(
            String code, String name, RoleType roleType, RoleStatus status, Integer pageNo, Integer pageSize) {
        int normalizedPageNo = PageParamNormalizer.normalizePageNo(pageNo);
        int normalizedPageSize = PageParamNormalizer.normalizePageSize(pageSize);
        return new PageResultDTO<>(
                roleRepository
                        .page(
                                RoleCodeCodec.toDomain(code),
                                name,
                                roleType,
                                status,
                                normalizedPageNo,
                                normalizedPageSize)
                        .stream()
                        .map(RoleAssembler::toDto)
                        .toList(),
                roleRepository.count(RoleCodeCodec.toDomain(code), name, roleType, status),
                normalizedPageNo,
                normalizedPageSize);
    }

    @Transactional
    public RoleDTO createRole(String code, String name, RoleType roleType, RoleDataScopeType dataScopeType) {
        validateRequired(code, "code");
        validateRequired(name, "name");
        return RoleAssembler.toDto(roleRepository.insert(Role.create(
                RoleIdCodec.toDomain(idGenerator.nextId(ROLE_ID_BIZ_TAG)),
                RoleCodeCodec.toDomain(code),
                trimPreservingNull(name),
                roleType,
                dataScopeType,
                RoleStatus.ACTIVE)));
    }

    @Transactional
    public RoleDTO updateRole(
            RoleId roleId, String code, String name, RoleType roleType, RoleDataScopeType dataScopeType) {
        Role currentRole = roleRepository
                .findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleId));
        validateRequired(code, "code");
        validateRequired(name, "name");
        currentRole.recodeAs(RoleCodeCodec.toDomain(code));
        currentRole.rename(trimPreservingNull(name));
        if (roleType != null) {
            currentRole.retypeAs(roleType);
        }
        if (dataScopeType != null) {
            currentRole.assignDataScope(dataScopeType, Set.of());
        }
        return RoleAssembler.toDto(roleRepository.update(currentRole));
    }

    @Transactional
    public RoleDTO updateRoleStatus(RoleId roleId, RoleStatus status) {
        Role role = roleRepository
                .findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleId));
        if (RoleStatus.ACTIVE == status) {
            role.activate();
        } else {
            role.disable();
        }
        return RoleAssembler.toDto(roleRepository.update(role));
    }

    @Transactional
    public void delete(RoleId roleId) {
        roleRepository
                .findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleId));
        roleRepository.delete(roleId);
    }

    public Set<String> getMenuIds(RoleId roleId) {
        return roleRepository.findMenuIds(roleId).stream()
                .map(MenuIdCodec::toValue)
                .map(String::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Transactional
    public Set<String> updateMenuIds(RoleId roleId, Set<MenuId> menuIds) {
        return roleRepository.updateMenuIds(roleId, menuIds).stream()
                .map(MenuIdCodec::toValue)
                .map(String::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> getResourceCodes(RoleId roleId) {
        return roleRepository.findResourceCodes(roleId).stream()
                .map(ResourceCodeCodec::toValue)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Transactional
    public Set<String> updateResourceCodes(RoleId roleId, Set<String> resourceCodes) {
        Set<ResourceCode> safeResourceCodes =
                resourceCodes == null
                        ? Set.of()
                        : resourceCodes.stream()
                                .map(ResourceCodeCodec::toDomain)
                                .collect(Collectors.toCollection(LinkedHashSet::new));
        return roleRepository.updateResourceCodes(roleId, safeResourceCodes).stream()
                .map(ResourceCodeCodec::toValue)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public RoleDataScopeAssignment getDataScope(RoleId roleId) {
        return roleRepository.findDataScope(roleId);
    }

    @Transactional
    public RoleDataScopeAssignment updateDataScope(
            RoleId roleId, RoleDataScopeType dataScopeType, Set<DepartmentId> departmentIds) {
        return roleRepository.updateDataScope(roleId, RoleDataScopeAssignment.of(dataScopeType, departmentIds));
    }

    private void validateRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " must not be blank");
        }
    }

    private String trimPreservingNull(String value) {
        return value == null ? null : value.trim();
    }
}
