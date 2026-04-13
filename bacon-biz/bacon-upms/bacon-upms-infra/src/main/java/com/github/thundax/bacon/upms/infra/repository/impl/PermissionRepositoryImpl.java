package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import com.github.thundax.bacon.upms.infra.cache.UpmsPermissionCacheSupport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class PermissionRepositoryImpl implements PermissionRepository {

    private final MenuRepositoryImpl menuRepository;
    private final RoleRepositoryImpl roleRepository;
    private final UpmsPermissionCacheSupport cacheSupport;

    public PermissionRepositoryImpl(
            MenuRepositoryImpl menuRepository,
            RoleRepositoryImpl roleRepository,
            UpmsPermissionCacheSupport cacheSupport) {
        this.menuRepository = menuRepository;
        this.roleRepository = roleRepository;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public List<Menu> listMenus(TenantId tenantId) {
        return cacheSupport.getTenantMenuTree(tenantId, () -> buildMenuTree(menuRepository.listMenus(tenantId)));
    }

    @Override
    public List<Menu> getUserMenuTree(TenantId tenantId, UserId userId) {
        return cacheSupport.getUserMenuTree(tenantId, userId, () -> loadUserMenuTree(tenantId, userId));
    }

    @Override
    public Set<String> getUserPermissionCodes(TenantId tenantId, UserId userId) {
        return cacheSupport.getUserPermissionCodes(tenantId, userId, () -> loadUserPermissionCodes(tenantId, userId));
    }

    @Override
    public Set<DepartmentId> getUserDepartmentIds(TenantId tenantId, UserId userId) {
        return cacheSupport.getUserDepartmentIds(tenantId, userId, () -> loadUserDepartmentIds(tenantId, userId));
    }

    @Override
    public Set<String> getUserScopeTypes(TenantId tenantId, UserId userId) {
        return cacheSupport.getUserScopeTypes(tenantId, userId, () -> loadUserScopeTypes(tenantId, userId));
    }

    @Override
    public boolean hasAllAccess(TenantId tenantId, UserId userId) {
        return getUserScopeTypes(tenantId, userId).contains("ALL");
    }

    private List<Menu> loadUserMenuTree(TenantId tenantId, UserId userId) {
        List<Role> roles = roleRepository.findRolesByUserId(tenantId, userId);
        if (roles.isEmpty()) {
            return List.of();
        }
        Set<MenuId> menuIds = new HashSet<>();
        roles.forEach(role -> menuIds.addAll(roleRepository.getAssignedMenus(role.getTenantId(), role.getId())));
        if (menuIds.isEmpty()) {
            return List.of();
        }
        List<Menu> menus = menuRepository.listMenus(tenantId).stream()
                .filter(menu -> menuIds.contains(menu.getId()))
                .toList();
        return buildMenuTree(menus);
    }

    private Set<String> loadUserPermissionCodes(TenantId tenantId, UserId userId) {
        List<Role> roles = roleRepository.findRolesByUserId(tenantId, userId);
        if (roles.isEmpty()) {
            return Set.of();
        }
        Map<MenuId, Menu> menuMap = menuRepository.listMenus(tenantId).stream()
                .collect(Collectors.toMap(Menu::getId, menu -> menu));
        Set<String> permissionCodes = new HashSet<>();
        roles.forEach(role -> {
            roleRepository.getAssignedMenus(role.getTenantId(), role.getId()).forEach(menuId -> {
                Menu menu = menuMap.get(menuId);
                if (menu != null
                        && menu.getPermissionCode() != null
                        && !menu.getPermissionCode().isBlank()) {
                    permissionCodes.add(menu.getPermissionCode());
                }
            });
            permissionCodes.addAll(roleRepository.getAssignedResources(role.getTenantId(), role.getId()));
        });
        return Set.copyOf(permissionCodes);
    }

    private Set<DepartmentId> loadUserDepartmentIds(TenantId tenantId, UserId userId) {
        Set<DepartmentId> departmentIds = new HashSet<>();
        roleRepository
                .findRolesByUserId(tenantId, userId)
                .forEach(role -> departmentIds.addAll(
                        roleRepository.getAssignedDataScopeDepartments(role.getTenantId(), role.getId())));
        return Set.copyOf(departmentIds);
    }

    private Set<String> loadUserScopeTypes(TenantId tenantId, UserId userId) {
        Set<String> scopeTypes = new HashSet<>();
        roleRepository
                .findRolesByUserId(tenantId, userId)
                .forEach(role ->
                        scopeTypes.add(roleRepository.getAssignedDataScopeType(role.getTenantId(), role.getId())));
        return Set.copyOf(scopeTypes);
    }

    private List<Menu> buildMenuTree(List<Menu> flatMenus) {
        Map<MenuId, Menu> menuMap = new HashMap<>();
        flatMenus.stream()
                .sorted(Comparator.comparing(Menu::getSort)
                        .thenComparing(menu -> menu.getId().value()))
                .forEach(menu -> menuMap.put(
                        menu.getId(),
                        Menu.reconstruct(
                                menu.getId(),
                                menu.getTenantId(),
                                menu.getMenuType(),
                                menu.getName(),
                                menu.getParentId(),
                                menu.getRoutePath(),
                                menu.getComponentName(),
                                menu.getIcon(),
                                menu.getSort(),
                                menu.getPermissionCode(),
                                new ArrayList<>())));

        List<Menu> roots = new ArrayList<>();
        menuMap.values().stream()
                .sorted(Comparator.comparing(Menu::getSort)
                        .thenComparing(menu -> menu.getId().value()))
                .forEach(menu -> {
                    if (menu.getParentId() == null || !menuMap.containsKey(menu.getParentId())) {
                        roots.add(menu);
                    } else {
                        menuMap.get(menu.getParentId()).getChildren().add(menu);
                    }
                });
        return roots;
    }
}
