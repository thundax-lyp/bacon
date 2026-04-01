package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.domain.MenuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class MenuRepositoryImpl implements MenuRepository {

    private final MenuPersistenceSupport support;
    private final RoleRepositoryImpl roleRepository;
    private final UpmsPermissionCacheSupport cacheSupport;
    private final Ids ids;

    public MenuRepositoryImpl(MenuPersistenceSupport support,
                              RoleRepositoryImpl roleRepository,
                              UpmsPermissionCacheSupport cacheSupport,
                              Ids ids) {
        this.support = support;
        this.roleRepository = roleRepository;
        this.cacheSupport = cacheSupport;
        this.ids = ids;
    }

    @Override
    public List<Menu> listMenus(TenantId tenantId) {
        return support.listMenus(tenantId);
    }

    @Override
    public Optional<Menu> findMenuById(TenantId tenantId, MenuId menuId) {
        return support.findMenuById(tenantId, menuId);
    }

    @Override
    public Menu save(Menu menu) {
        Menu menuToSave = menu.getId() == null
                ? new Menu(ids.menuId(), menu.getTenantId(), menu.getMenuType(), menu.getName(), menu.getParentId(),
                menu.getRoutePath(), menu.getComponentName(), menu.getIcon(), menu.getSort(), menu.getPermissionCode(),
                menu.getChildren())
                : menu;
        Menu savedMenu = support.saveMenu(menuToSave);
        cacheSupport.evictTenantPermission(savedMenu.getTenantId());
        return savedMenu;
    }

    @Override
    public Menu updateSort(TenantId tenantId, MenuId menuId, Integer sort) {
        Menu currentMenu = findMenuById(tenantId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));
        return support.saveMenu(new Menu(currentMenu.getId(), currentMenu.getTenantId(), currentMenu.getMenuType(),
                currentMenu.getName(), currentMenu.getParentId(), currentMenu.getRoutePath(), currentMenu.getComponentName(),
                currentMenu.getIcon(), sort, currentMenu.getPermissionCode(), List.of()));
    }

    @Override
    public void deleteMenu(TenantId tenantId, MenuId menuId) {
        support.deleteMenu(tenantId, menuId);
        roleRepository.removeMenuFromAssignments(tenantId, menuId);
        cacheSupport.evictTenantPermission(tenantId);
    }

    @Override
    public boolean existsChildMenu(TenantId tenantId, MenuId menuId) {
        return support.existsChildMenu(tenantId, menuId);
    }
}
