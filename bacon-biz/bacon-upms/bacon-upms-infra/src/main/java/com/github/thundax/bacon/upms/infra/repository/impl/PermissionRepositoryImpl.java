package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.entity.Menu;
import com.github.thundax.bacon.upms.domain.entity.Role;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class PermissionRepositoryImpl implements PermissionRepository {

    private final InMemoryUpmsStore upmsStore;

    public PermissionRepositoryImpl(InMemoryUpmsStore upmsStore) {
        this.upmsStore = upmsStore;
    }

    @Override
    public List<Menu> listMenus(Long tenantId) {
        return buildMenuTree(upmsStore.getMenus().values().stream()
                .filter(menu -> menu.getTenantId().equals(tenantId))
                .toList());
    }

    @Override
    public List<Menu> getUserMenuTree(Long tenantId, Long userId) {
        List<Role> roles = upmsStore.getUserRoles().getOrDefault(InMemoryUpmsStore.userKey(tenantId, userId), List.of());
        if (roles.isEmpty()) {
            return upmsStore.getUserMenus().getOrDefault(InMemoryUpmsStore.userKey(tenantId, userId), List.of());
        }
        Set<Long> menuIds = new HashSet<>();
        roles.forEach(role -> menuIds.addAll(upmsStore.getRoleMenus()
                .getOrDefault(InMemoryUpmsStore.roleKey(role.getTenantId(), role.getId()), Set.of())));
        List<Menu> menus = menuIds.stream()
                .map(menuId -> upmsStore.getMenus().get(InMemoryUpmsStore.menuKey(tenantId, menuId)))
                .filter(java.util.Objects::nonNull)
                .toList();
        return buildMenuTree(menus);
    }

    @Override
    public Set<String> getUserPermissionCodes(Long tenantId, Long userId) {
        List<Role> roles = upmsStore.getUserRoles().getOrDefault(InMemoryUpmsStore.userKey(tenantId, userId), List.of());
        if (roles.isEmpty()) {
            return upmsStore.getUserPermissions().getOrDefault(InMemoryUpmsStore.userKey(tenantId, userId), Set.of());
        }
        Set<String> permissionCodes = new HashSet<>();
        roles.forEach(role -> {
            String roleKey = InMemoryUpmsStore.roleKey(role.getTenantId(), role.getId());
            upmsStore.getRoleMenus().getOrDefault(roleKey, Set.of()).forEach(menuId -> {
                Menu menu = upmsStore.getMenus().get(InMemoryUpmsStore.menuKey(tenantId, menuId));
                if (menu != null && menu.getPermissionCode() != null && !menu.getPermissionCode().isBlank()) {
                    permissionCodes.add(menu.getPermissionCode());
                }
            });
            permissionCodes.addAll(upmsStore.getRoleResources().getOrDefault(roleKey, Set.of()));
        });
        return permissionCodes;
    }

    @Override
    public Set<Long> getUserDepartmentIds(Long tenantId, Long userId) {
        return upmsStore.getUserDepartmentScopes().getOrDefault(InMemoryUpmsStore.userKey(tenantId, userId), Set.of());
    }

    @Override
    public Set<String> getUserScopeTypes(Long tenantId, Long userId) {
        return upmsStore.getUserScopeTypes().getOrDefault(InMemoryUpmsStore.userKey(tenantId, userId), Set.of());
    }

    @Override
    public boolean hasAllAccess(Long tenantId, Long userId) {
        return upmsStore.getUserAllAccess().getOrDefault(InMemoryUpmsStore.userKey(tenantId, userId), false);
    }

    private List<Menu> buildMenuTree(List<Menu> flatMenus) {
        Map<Long, Menu> menuMap = new HashMap<>();
        flatMenus.stream()
                .sorted(Comparator.comparing(Menu::getSort).thenComparing(Menu::getId))
                .forEach(menu -> menuMap.put(menu.getId(), new Menu(menu.getId(), menu.getTenantId(), menu.getMenuType(),
                        menu.getName(), menu.getParentId(), menu.getRoutePath(), menu.getComponentName(), menu.getIcon(),
                        menu.getSort(), menu.getPermissionCode(), new ArrayList<>())));

        List<Menu> roots = new ArrayList<>();
        menuMap.values().stream()
                .sorted(Comparator.comparing(Menu::getSort).thenComparing(Menu::getId))
                .forEach(menu -> {
                    if (menu.getParentId() == null || menu.getParentId() == 0L || !menuMap.containsKey(menu.getParentId())) {
                        roots.add(menu);
                    } else {
                        menuMap.get(menu.getParentId()).getChildren().add(menu);
                    }
                });
        return roots;
    }
}
