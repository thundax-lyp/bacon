package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class MenuRepositoryImpl implements MenuRepository {

    private final Map<String, Menu> menus = new ConcurrentHashMap<>();
    private final AtomicLong menuIdSequence = new AtomicLong(5003L);
    private final RoleRepositoryImpl roleRepository;

    public MenuRepositoryImpl(RoleRepositoryImpl roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Menu> listMenus(Long tenantId) {
        return menus.values().stream()
                .filter(menu -> menu.getTenantId().equals(tenantId))
                .sorted(Comparator.comparing(Menu::getSort).thenComparing(Menu::getId))
                .toList();
    }

    @Override
    public Optional<Menu> findMenuById(Long tenantId, Long menuId) {
        return Optional.ofNullable(menus.get(UpmsRepositoryHelper.menuKey(tenantId, menuId)));
    }

    @Override
    public Menu save(Menu menu) {
        Menu savedMenu = menu.getId() == null
                ? new Menu(menuIdSequence.getAndIncrement(), menu.getTenantId(), menu.getMenuType(), menu.getName(), menu.getParentId(),
                menu.getRoutePath(), menu.getComponentName(), menu.getIcon(), menu.getSort(), menu.getPermissionCode(),
                List.of())
                : new Menu(menu.getId(), menu.getTenantId(), menu.getMenuType(), menu.getName(), menu.getParentId(),
                menu.getRoutePath(), menu.getComponentName(), menu.getIcon(), menu.getSort(), menu.getPermissionCode(),
                List.of());
        menus.put(UpmsRepositoryHelper.menuKey(savedMenu.getTenantId(), savedMenu.getId()), savedMenu);
        return savedMenu;
    }

    @Override
    public Menu updateSort(Long tenantId, Long menuId, Integer sort) {
        Menu currentMenu = findMenuById(tenantId, menuId)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + menuId));
        Menu updatedMenu = new Menu(currentMenu.getId(), currentMenu.getTenantId(), currentMenu.getMenuType(),
                currentMenu.getName(), currentMenu.getParentId(), currentMenu.getRoutePath(), currentMenu.getComponentName(),
                currentMenu.getIcon(), sort, currentMenu.getPermissionCode(), List.of());
        menus.put(UpmsRepositoryHelper.menuKey(tenantId, menuId), updatedMenu);
        return updatedMenu;
    }

    @Override
    public void deleteMenu(Long tenantId, Long menuId) {
        menus.remove(UpmsRepositoryHelper.menuKey(tenantId, menuId));
        roleRepository.removeMenuFromAssignments(tenantId, menuId);
    }

    @Override
    public boolean existsChildMenu(Long tenantId, Long menuId) {
        return menus.values().stream()
                .filter(menu -> menu.getTenantId().equals(tenantId))
                .anyMatch(menu -> menuId.equals(menu.getParentId()));
    }
}
