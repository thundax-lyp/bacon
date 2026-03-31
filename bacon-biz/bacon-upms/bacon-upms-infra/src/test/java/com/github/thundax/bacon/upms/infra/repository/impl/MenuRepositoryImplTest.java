package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MenuRepositoryImplTest {

    @Test
    void shouldRemoveRoleMenuAssignmentsWhenMenuDeleted() {
        RoleRepositoryImpl roleRepository = new RoleRepositoryImpl();
        MenuRepositoryImpl menuRepository = new MenuRepositoryImpl(roleRepository);
        Role adminRole = roleRepository.save(new Role(null, 1L, "ADMIN", "Admin", "SYSTEM", "SELF", "ACTIVE"));
        Menu rootMenu = menuRepository.save(new Menu(null, 1L, "CATALOG", "System", 0L,
                "/system", "SystemPage", "setting", 1, null, List.of()));
        Menu childMenu = menuRepository.save(new Menu(null, 1L, "MENU", "User", rootMenu.getId(),
                "/system/user", "UserPage", "user", 2, "upms:user:list", List.of()));

        roleRepository.assignMenus(1L, adminRole.getId(), Set.of(rootMenu.getId(), childMenu.getId()));
        assertThat(menuRepository.existsChildMenu(1L, rootMenu.getId())).isTrue();

        menuRepository.deleteMenu(1L, childMenu.getId());

        assertThat(menuRepository.findMenuById(1L, childMenu.getId())).isEmpty();
        assertThat(roleRepository.getAssignedMenus(1L, adminRole.getId())).containsExactly(rootMenu.getId());
    }
}
