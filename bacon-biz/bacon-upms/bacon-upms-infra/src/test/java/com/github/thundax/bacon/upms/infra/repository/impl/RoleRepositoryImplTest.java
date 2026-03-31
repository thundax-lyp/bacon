package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Role;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RoleRepositoryImplTest {

    @Test
    void shouldPersistAssignmentsAndCleanUserRelationsWhenRoleDeleted() {
        RoleRepositoryImpl roleRepository = new RoleRepositoryImpl();
        Role savedRole = roleRepository.save(new Role(null, 1L, "ADMIN", "Admin", "SYSTEM", "SELF", "ACTIVE"));

        roleRepository.bindUserRoles(1L, 100L, List.of(savedRole));
        roleRepository.assignMenus(1L, savedRole.getId(), Set.of(11L, 12L));
        roleRepository.assignResources(1L, savedRole.getId(), Set.of("upms:user:view"));
        roleRepository.assignDataScope(1L, savedRole.getId(), "CUSTOM", Set.of(21L, 22L));

        assertThat(roleRepository.findRolesByUserId(1L, 100L)).extracting(Role::getCode).containsExactly("ADMIN");
        assertThat(roleRepository.getAssignedMenus(1L, savedRole.getId())).containsExactlyInAnyOrder(11L, 12L);
        assertThat(roleRepository.getAssignedResources(1L, savedRole.getId())).containsExactly("upms:user:view");
        assertThat(roleRepository.getAssignedDataScopeType(1L, savedRole.getId())).isEqualTo("CUSTOM");
        assertThat(roleRepository.getAssignedDataScopeDepartments(1L, savedRole.getId())).containsExactlyInAnyOrder(21L, 22L);

        roleRepository.deleteRole(1L, savedRole.getId());

        assertThat(roleRepository.findRoleById(1L, savedRole.getId())).isEmpty();
        assertThat(roleRepository.findRolesByUserId(1L, 100L)).isEmpty();
    }
}
