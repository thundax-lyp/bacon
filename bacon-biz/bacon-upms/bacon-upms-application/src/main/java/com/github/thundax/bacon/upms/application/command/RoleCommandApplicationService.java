package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.core.exception.BadRequestException;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.upms.application.assembler.RoleAssembler;
import com.github.thundax.bacon.upms.application.codec.MenuIdCodec;
import com.github.thundax.bacon.upms.application.codec.ResourceCodeCodec;
import com.github.thundax.bacon.upms.application.codec.RoleIdCodec;
import com.github.thundax.bacon.upms.application.dto.RoleDTO;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import com.github.thundax.bacon.upms.domain.repository.RoleDataScopeRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleMenuRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleResourceRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleCommandApplicationService {

    private static final String ROLE_ID_BIZ_TAG = "role-id";

    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final RoleResourceRepository roleResourceRepository;
    private final RoleDataScopeRepository roleDataScopeRepository;
    private final IdGenerator idGenerator;

    public RoleCommandApplicationService(
            RoleRepository roleRepository,
            RoleMenuRepository roleMenuRepository,
            RoleResourceRepository roleResourceRepository,
            RoleDataScopeRepository roleDataScopeRepository,
            IdGenerator idGenerator) {
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.roleResourceRepository = roleResourceRepository;
        this.roleDataScopeRepository = roleDataScopeRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public RoleDTO create(RoleCreateCommand command) {
        validateRequired(command.name(), "name");
        if (command.code() == null) {
            throw new BadRequestException("code must not be null");
        }
        return RoleAssembler.toDto(roleRepository.insert(Role.create(
                RoleIdCodec.toDomain(idGenerator.nextId(ROLE_ID_BIZ_TAG)),
                command.code(),
                trimPreservingNull(command.name()),
                command.roleType(),
                command.dataScopeType())));
    }

    @Transactional
    public RoleDTO update(RoleUpdateCommand command) {
        Role currentRole = requireRole(command.roleId());
        validateRequired(command.name(), "name");
        if (command.code() == null) {
            throw new BadRequestException("code must not be null");
        }
        currentRole.recodeAs(command.code());
        currentRole.rename(trimPreservingNull(command.name()));
        if (command.roleType() != null) {
            currentRole.retypeAs(command.roleType());
        }
        if (command.dataScopeType() != null) {
            currentRole.assignDataScope(command.dataScopeType(), Set.of());
        }
        return RoleAssembler.toDto(roleRepository.update(currentRole));
    }

    @Transactional
    public RoleDTO updateStatus(RoleStatusUpdateCommand command) {
        Role role = requireRole(command.roleId());
        if (command.status() == null) {
            throw new BadRequestException("status must not be null");
        }
        if (RoleStatus.ACTIVE == command.status()) {
            role.activate();
        } else {
            role.disable();
        }
        return RoleAssembler.toDto(roleRepository.update(role));
    }

    @Transactional
    public void delete(RoleId roleId) {
        requireRole(roleId);
        roleRepository.delete(roleId);
    }

    @Transactional
    public Set<String> updateMenuIds(RoleMenuAssignCommand command) {
        return roleMenuRepository.updateMenuIds(command.roleId(), command.menuIds()).stream()
                .map(MenuIdCodec::toValue)
                .map(String::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Transactional
    public Set<String> updateResourceCodes(RoleResourceAssignCommand command) {
        Set<ResourceCode> safeResourceCodes =
                command.resourceCodes() == null
                        ? Set.of()
                        : command.resourceCodes().stream()
                                .collect(Collectors.toCollection(LinkedHashSet::new));
        return roleResourceRepository.updateResourceCodes(command.roleId(), safeResourceCodes).stream()
                .map(ResourceCodeCodec::toValue)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Transactional
    public Set<DepartmentId> updateDataScope(RoleDataScopeAssignCommand command) {
        return roleDataScopeRepository.updateDataScope(
                command.roleId(), command.dataScopeType(), command.departmentIds());
    }

    private Role requireRole(RoleId roleId) {
        return roleRepository
                .findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleId));
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
