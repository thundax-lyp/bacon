package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionRepositoryImplTest {

    @Test
    void shouldAssembleMenuTreeAndPermissionViewFromRoleAssignments() {
        RoleRepositoryImpl roleRepository = new RoleRepositoryImpl();
        MenuRepositoryImpl menuRepository = new MenuRepositoryImpl(roleRepository);
        PermissionRepositoryImpl permissionRepository = new PermissionRepositoryImpl(menuRepository, roleRepository);

        Role adminRole = roleRepository.save(new Role(null, 1L, "ADMIN", "Admin", "SYSTEM", "ALL", "ACTIVE"));
        Menu rootMenu = menuRepository.save(new Menu(null, 1L, "CATALOG", "System", 0L,
                "/system", "SystemPage", "setting", 1, null, List.of()));
        Menu childMenu = menuRepository.save(new Menu(null, 1L, "MENU", "User", rootMenu.getId(),
                "/system/user", "UserPage", "user", 2, "upms:user:list", List.of()));

        roleRepository.bindUserRoles(1L, 100L, List.of(adminRole));
        roleRepository.assignMenus(1L, adminRole.getId(), Set.of(rootMenu.getId(), childMenu.getId()));
        roleRepository.assignResources(1L, adminRole.getId(), Set.of("upms:user:view"));
        roleRepository.assignDataScope(1L, adminRole.getId(), "ALL", Set.of(10L, 20L));

        List<Menu> menuTree = permissionRepository.getUserMenuTree(1L, 100L);

        assertThat(menuTree).hasSize(1);
        assertThat(menuTree.get(0).getName()).isEqualTo("System");
        assertThat(menuTree.get(0).getChildren()).extracting(Menu::getName).containsExactly("User");
        assertThat(permissionRepository.getUserPermissionCodes(1L, 100L))
                .containsExactlyInAnyOrder("upms:user:list", "upms:user:view");
        assertThat(permissionRepository.getUserDepartmentIds(1L, 100L)).containsExactlyInAnyOrder(10L, 20L);
        assertThat(permissionRepository.getUserScopeTypes(1L, 100L)).containsExactly("ALL");
        assertThat(permissionRepository.hasAllAccess(1L, 100L)).isTrue();
    }
}
