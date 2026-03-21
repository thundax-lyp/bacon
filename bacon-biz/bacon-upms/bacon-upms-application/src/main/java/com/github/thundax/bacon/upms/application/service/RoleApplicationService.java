package com.github.thundax.bacon.upms.application.service;

import com.github.thundax.bacon.upms.api.dto.RoleDTO;
import com.github.thundax.bacon.upms.domain.entity.Role;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    private RoleDTO toDto(Role role) {
        return new RoleDTO(role.getId(), role.getTenantId(), role.getCode(), role.getName(),
                role.getRoleType(), role.getDataScopeType(), role.getStatus());
    }
}
