package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class PermissionRepositoryImpl implements PermissionRepository {

    private final MenuRepositoryImpl menuRepository;
    private final RoleRepositoryImpl roleRepository;

    public PermissionRepositoryImpl(MenuRepositoryImpl menuRepository, RoleRepositoryImpl roleRepository) {
        this.menuRepository = menuRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Menu> listMenus(Long tenantId) {
        return buildMenuTree(menuRepository.listMenus(tenantId));
    }

    @Override
    public List<Menu> getUserMenuTree(Long tenantId, Long userId) {
        List<com.github.thundax.bacon.upms.domain.model.entity.Role> roles = roleRepository.findRolesByUserId(tenantId, userId);
        if (roles.isEmpty()) {
            return List.of();
        }
        Set<Long> menuIds = new HashSet<>();
        roles.forEach(role -> menuIds.addAll(roleRepository.getAssignedMenus(role.getTenantId(), role.getId())));
        List<Menu> menus = menuIds.stream()
                .map(menuId -> menuRepository.findMenuById(tenantId, menuId).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
        return buildMenuTree(menus);
    }

    @Override
    public Set<String> getUserPermissionCodes(Long tenantId, Long userId) {
        List<com.github.thundax.bacon.upms.domain.model.entity.Role> roles = roleRepository.findRolesByUserId(tenantId, userId);
        if (roles.isEmpty()) {
            return Set.of();
        }
        Set<String> permissionCodes = new HashSet<>();
        roles.forEach(role -> {
            roleRepository.getAssignedMenus(role.getTenantId(), role.getId()).forEach(menuId -> {
                Menu menu = menuRepository.findMenuById(tenantId, menuId).orElse(null);
                if (menu != null && menu.getPermissionCode() != null && !menu.getPermissionCode().isBlank()) {
                    permissionCodes.add(menu.getPermissionCode());
                }
            });
            permissionCodes.addAll(roleRepository.getAssignedResources(role.getTenantId(), role.getId()));
        });
        return permissionCodes;
    }

    @Override
    public Set<Long> getUserDepartmentIds(Long tenantId, Long userId) {
        Set<Long> departmentIds = new HashSet<>();
        roleRepository.findRolesByUserId(tenantId, userId)
                .forEach(role -> departmentIds.addAll(roleRepository.getAssignedDataScopeDepartments(role.getTenantId(), role.getId())));
        return departmentIds;
    }

    @Override
    public Set<String> getUserScopeTypes(Long tenantId, Long userId) {
        Set<String> scopeTypes = new HashSet<>();
        roleRepository.findRolesByUserId(tenantId, userId)
                .forEach(role -> scopeTypes.add(roleRepository.getAssignedDataScopeType(role.getTenantId(), role.getId())));
        return scopeTypes;
    }

    @Override
    public boolean hasAllAccess(Long tenantId, Long userId) {
        return getUserScopeTypes(tenantId, userId).contains("ALL");
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
