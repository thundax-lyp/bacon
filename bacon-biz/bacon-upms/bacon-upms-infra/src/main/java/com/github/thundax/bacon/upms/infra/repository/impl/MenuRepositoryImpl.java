package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean({MenuPersistenceSupport.class, RolePersistenceSupport.class})
public class MenuRepositoryImpl implements MenuRepository {

    private final MenuPersistenceSupport support;
    private final RoleRepositoryImpl roleRepository;
    private final UpmsPermissionCacheSupport cacheSupport;

    public MenuRepositoryImpl(MenuPersistenceSupport support,
                              RoleRepositoryImpl roleRepository,
                              UpmsPermissionCacheSupport cacheSupport) {
        this.support = support;
        this.roleRepository = roleRepository;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public List<Menu> listMenus(TenantId tenantId) {
        return support.listMenus(tenantId);
    }

    @Override
    public Optional<Menu> findMenuById(TenantId tenantId, Long menuId) {
        return support.findMenuById(tenantId, menuId);
    }

    @Override
    public Menu save(Menu menu) {
        Menu savedMenu = support.saveMenu(menu);
        cacheSupport.evictTenantPermission(savedMenu.getTenantId());
        return savedMenu;
    }

    @Override
    public Menu updateSort(TenantId tenantId, Long menuId, Integer sort) {
        Menu currentMenu = findMenuById(tenantId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));
        return support.saveMenu(new Menu(currentMenu.getId(), currentMenu.getTenantId(), currentMenu.getMenuType(),
                currentMenu.getName(), currentMenu.getParentId(), currentMenu.getRoutePath(), currentMenu.getComponentName(),
                currentMenu.getIcon(), sort, currentMenu.getPermissionCode(), List.of()));
    }

    @Override
    public void deleteMenu(TenantId tenantId, Long menuId) {
        support.deleteMenu(tenantId, menuId);
        roleRepository.removeMenuFromAssignments(tenantId, menuId);
        cacheSupport.evictTenantPermission(tenantId);
    }

    @Override
    public boolean existsChildMenu(TenantId tenantId, Long menuId) {
        return support.existsChildMenu(tenantId, menuId);
    }
}
