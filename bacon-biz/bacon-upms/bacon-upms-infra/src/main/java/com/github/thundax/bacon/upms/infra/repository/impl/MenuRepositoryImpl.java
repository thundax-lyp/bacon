package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(UpmsRepositorySupport.class)
public class MenuRepositoryImpl implements MenuRepository {

    private final UpmsRepositorySupport support;
    private final RoleRepositoryImpl roleRepository;

    public MenuRepositoryImpl(UpmsRepositorySupport support, RoleRepositoryImpl roleRepository) {
        this.support = support;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Menu> listMenus(Long tenantId) {
        return support.listMenus(tenantId);
    }

    @Override
    public Optional<Menu> findMenuById(Long tenantId, Long menuId) {
        return support.findMenuById(tenantId, menuId);
    }

    @Override
    public Menu save(Menu menu) {
        return support.saveMenu(menu);
    }

    @Override
    public Menu updateSort(Long tenantId, Long menuId, Integer sort) {
        Menu currentMenu = findMenuById(tenantId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));
        return support.saveMenu(new Menu(currentMenu.getId(), currentMenu.getTenantId(), currentMenu.getMenuType(),
                currentMenu.getName(), currentMenu.getParentId(), currentMenu.getRoutePath(), currentMenu.getComponentName(),
                currentMenu.getIcon(), sort, currentMenu.getPermissionCode(), List.of()));
    }

    @Override
    public void deleteMenu(Long tenantId, Long menuId) {
        support.deleteMenu(tenantId, menuId);
        roleRepository.removeMenuFromAssignments(tenantId, menuId);
    }

    @Override
    public boolean existsChildMenu(Long tenantId, Long menuId) {
        return support.existsChildMenu(tenantId, menuId);
    }
}
