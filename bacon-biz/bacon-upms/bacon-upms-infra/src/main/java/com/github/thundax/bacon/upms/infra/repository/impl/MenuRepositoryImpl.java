package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.exception.NotFoundException;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
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

    public MenuRepositoryImpl(
            MenuPersistenceSupport support,
            RoleRepositoryImpl roleRepository,
            UpmsPermissionCacheSupport cacheSupport) {
        this.support = support;
        this.roleRepository = roleRepository;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public List<Menu> listMenus() {
        return support.listMenus();
    }

    @Override
    public Optional<Menu> findMenuById(MenuId menuId) {
        return support.findMenuById(menuId);
    }

    @Override
    public Menu insert(Menu menu) {
        TenantId tenantId = requireTenantId();
        Menu savedMenu = support.insertMenu(menu);
        cacheSupport.evictTenantPermission(tenantId);
        return savedMenu;
    }

    @Override
    public Menu update(Menu menu) {
        TenantId tenantId = requireTenantId();
        Menu savedMenu = support.updateMenu(menu);
        cacheSupport.evictTenantPermission(tenantId);
        return savedMenu;
    }

    @Override
    public Menu updateSort(MenuId menuId, Integer sort) {
        TenantId tenantId = requireTenantId();
        Menu currentMenu =
                findMenuById(menuId).orElseThrow(() -> new NotFoundException("Menu not found: " + menuId));
        currentMenu.sort(sort);
        Menu savedMenu = support.updateMenu(currentMenu);
        cacheSupport.evictTenantPermission(tenantId);
        return savedMenu;
    }

    @Override
    public void deleteMenu(MenuId menuId) {
        TenantId tenantId = requireTenantId();
        support.deleteMenu(menuId);
        roleRepository.removeMenuFromAssignments(menuId);
        cacheSupport.evictTenantPermission(tenantId);
    }

    @Override
    public boolean existsChildMenu(MenuId menuId) {
        return support.existsChildMenu(menuId);
    }

    private TenantId requireTenantId() {
        return TenantId.of(BaconContextHolder.requireTenantId());
    }
}
