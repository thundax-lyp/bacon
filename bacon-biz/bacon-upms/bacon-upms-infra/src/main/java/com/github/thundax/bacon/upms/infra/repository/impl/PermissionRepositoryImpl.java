package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean({MenuPersistenceSupport.class, RolePersistenceSupport.class})
public class PermissionRepositoryImpl implements PermissionRepository {

    private final MenuRepositoryImpl menuRepository;
    private final RoleRepositoryImpl roleRepository;
    private final UpmsPermissionCacheSupport cacheSupport;

    public PermissionRepositoryImpl(MenuRepositoryImpl menuRepository,
                                    RoleRepositoryImpl roleRepository,
                                    UpmsPermissionCacheSupport cacheSupport) {
        this.menuRepository = menuRepository;
        this.roleRepository = roleRepository;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public List<Menu> listMenus(Long tenantId) {
        return cacheSupport.getTenantMenuTree(tenantId, () -> buildMenuTree(menuRepository.listMenus(tenantId)));
    }

    @Override
    public List<Menu> getUserMenuTree(Long tenantId, UserId userId) {
        return cacheSupport.getUserMenuTree(tenantId, userId, () -> loadUserMenuTree(tenantId, userId));
    }

    @Override
    public Set<String> getUserPermissionCodes(Long tenantId, UserId userId) {
        return cacheSupport.getUserPermissionCodes(tenantId, userId, () -> loadUserPermissionCodes(tenantId, userId));
    }

    @Override
    public Set<Long> getUserDepartmentIds(Long tenantId, UserId userId) {
        return cacheSupport.getUserDepartmentIds(tenantId, userId, () -> loadUserDepartmentIds(tenantId, userId));
    }

    @Override
    public Set<String> getUserScopeTypes(Long tenantId, UserId userId) {
        return cacheSupport.getUserScopeTypes(tenantId, userId, () -> loadUserScopeTypes(tenantId, userId));
    }

    @Override
    public boolean hasAllAccess(Long tenantId, UserId userId) {
        return getUserScopeTypes(tenantId, userId).contains("ALL");
    }

    private List<Menu> loadUserMenuTree(Long tenantId, UserId userId) {
        List<com.github.thundax.bacon.upms.domain.model.entity.Role> roles = roleRepository.findRolesByUserId(tenantId, userId);
        if (roles.isEmpty()) {
            return List.of();
        }
        Set<Long> menuIds = new HashSet<>();
        roles.forEach(role -> menuIds.addAll(roleRepository.getAssignedMenus(role.getTenantId(), role.getId())));
        if (menuIds.isEmpty()) {
            return List.of();
        }
        List<Menu> menus = menuRepository.listMenus(tenantId).stream()
                .filter(menu -> menuIds.contains(menu.getId()))
                .toList();
        return buildMenuTree(menus);
    }

    private Set<String> loadUserPermissionCodes(Long tenantId, UserId userId) {
        List<com.github.thundax.bacon.upms.domain.model.entity.Role> roles = roleRepository.findRolesByUserId(tenantId, userId);
        if (roles.isEmpty()) {
            return Set.of();
        }
        Map<Long, Menu> menuMap = menuRepository.listMenus(tenantId).stream()
                .collect(java.util.stream.Collectors.toMap(Menu::getId, menu -> menu));
        Set<String> permissionCodes = new HashSet<>();
        roles.forEach(role -> {
            roleRepository.getAssignedMenus(role.getTenantId(), role.getId()).forEach(menuId -> {
                Menu menu = menuMap.get(menuId);
                if (menu != null && menu.getPermissionCode() != null && !menu.getPermissionCode().isBlank()) {
                    permissionCodes.add(menu.getPermissionCode());
                }
            });
            permissionCodes.addAll(roleRepository.getAssignedResources(role.getTenantId(), role.getId()));
        });
        return Set.copyOf(permissionCodes);
    }

    private Set<Long> loadUserDepartmentIds(Long tenantId, UserId userId) {
        Set<Long> departmentIds = new HashSet<>();
        roleRepository.findRolesByUserId(tenantId, userId)
                .forEach(role -> departmentIds.addAll(roleRepository.getAssignedDataScopeDepartments(role.getTenantId(), role.getId())));
        return Set.copyOf(departmentIds);
    }

    private Set<String> loadUserScopeTypes(Long tenantId, UserId userId) {
        Set<String> scopeTypes = new HashSet<>();
        roleRepository.findRolesByUserId(tenantId, userId)
                .forEach(role -> scopeTypes.add(roleRepository.getAssignedDataScopeType(role.getTenantId(), role.getId())));
        return Set.copyOf(scopeTypes);
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
