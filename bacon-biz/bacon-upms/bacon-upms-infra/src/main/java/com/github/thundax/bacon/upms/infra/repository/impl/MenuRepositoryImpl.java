package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.entity.Menu;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MenuRepositoryImpl implements MenuRepository {

    private final InMemoryUpmsStore upmsStore;

    public MenuRepositoryImpl(InMemoryUpmsStore upmsStore) {
        this.upmsStore = upmsStore;
    }

    @Override
    public List<Menu> listMenus(Long tenantId) {
        return upmsStore.getMenus().values().stream()
                .filter(menu -> menu.getTenantId().equals(tenantId))
                .sorted(Comparator.comparing(Menu::getSort).thenComparing(Menu::getId))
                .toList();
    }

    @Override
    public Optional<Menu> findMenuById(Long tenantId, Long menuId) {
        return Optional.ofNullable(upmsStore.getMenus().get(InMemoryUpmsStore.menuKey(tenantId, menuId)));
    }

    @Override
    public Menu save(Menu menu) {
        Menu savedMenu = menu.getId() == null
                ? new Menu(upmsStore.nextMenuId(), menu.getTenantId(), menu.getMenuType(), menu.getName(), menu.getParentId(),
                menu.getRoutePath(), menu.getComponentName(), menu.getIcon(), menu.getSort(), menu.getPermissionCode(),
                List.of())
                : new Menu(menu.getId(), menu.getTenantId(), menu.getMenuType(), menu.getName(), menu.getParentId(),
                menu.getRoutePath(), menu.getComponentName(), menu.getIcon(), menu.getSort(), menu.getPermissionCode(),
                List.of());
        upmsStore.getMenus().put(InMemoryUpmsStore.menuKey(savedMenu.getTenantId(), savedMenu.getId()), savedMenu);
        return savedMenu;
    }

    @Override
    public Menu updateSort(Long tenantId, Long menuId, Integer sort) {
        Menu currentMenu = findMenuById(tenantId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));
        Menu updatedMenu = new Menu(currentMenu.getId(), currentMenu.getTenantId(), currentMenu.getMenuType(),
                currentMenu.getName(), currentMenu.getParentId(), currentMenu.getRoutePath(), currentMenu.getComponentName(),
                currentMenu.getIcon(), sort, currentMenu.getPermissionCode(), List.of());
        upmsStore.getMenus().put(InMemoryUpmsStore.menuKey(tenantId, menuId), updatedMenu);
        return updatedMenu;
    }

    @Override
    public void deleteMenu(Long tenantId, Long menuId) {
        upmsStore.getMenus().remove(InMemoryUpmsStore.menuKey(tenantId, menuId));
        upmsStore.getRoleMenus().replaceAll((key, menuIds) -> menuIds.stream().filter(id -> !id.equals(menuId)).collect(java.util.stream.Collectors.toSet()));
    }

    @Override
    public boolean existsChildMenu(Long tenantId, Long menuId) {
        return upmsStore.getMenus().values().stream()
                .filter(menu -> menu.getTenantId().equals(tenantId))
                .anyMatch(menu -> menuId.equals(menu.getParentId()));
    }
}
