package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
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
    public List<Menu> listMenus() {
        TenantId tenantId = requireTenantId();
        return cacheSupport.getTenantMenuTree(tenantId, () -> buildMenuTree(menuRepository.listMenus()));
    }

    @Override
    public List<Menu> getUserMenuTree(UserId userId) {
        TenantId tenantId = requireTenantId();
        return cacheSupport.getUserMenuTree(tenantId, userId, () -> loadUserMenuTree(userId));
    }

    @Override
    public Set<String> getUserPermissionCodes(UserId userId) {
        TenantId tenantId = requireTenantId();
        return cacheSupport.getUserPermissionCodes(tenantId, userId, () -> loadUserPermissionCodes(userId));
    }

    @Override
    public Set<DepartmentId> getUserDepartmentIds(UserId userId) {
        TenantId tenantId = requireTenantId();
        return cacheSupport.getUserDepartmentIds(tenantId, userId, () -> loadUserDepartmentIds(userId));
    }

    @Override
    public Set<String> getUserScopeTypes(UserId userId) {
        TenantId tenantId = requireTenantId();
        return cacheSupport.getUserScopeTypes(tenantId, userId, () -> loadUserScopeTypes(userId));
    }

    @Override
    public boolean hasAllAccess(UserId userId) {
        return getUserScopeTypes(userId).contains("ALL");
    }

    private List<Menu> loadUserMenuTree(UserId userId) {
        List<Role> roles = roleRepository.findRolesByUserId(userId);
        if (roles.isEmpty()) {
            return List.of();
        }
        Set<MenuId> menuIds = new HashSet<>();
        roles.forEach(role -> menuIds.addAll(roleRepository.getAssignedMenus(role.getId())));
        if (menuIds.isEmpty()) {
            return List.of();
        }
        List<Menu> menus = menuRepository.listMenus().stream()
                .filter(menu -> menuIds.contains(menu.getId()))
                .toList();
        return buildMenuTree(menus);
    }

    private Set<String> loadUserPermissionCodes(UserId userId) {
        List<Role> roles = roleRepository.findRolesByUserId(userId);
        if (roles.isEmpty()) {
            return Set.of();
        }
        Map<MenuId, Menu> menuMap =
                menuRepository.listMenus().stream().collect(Collectors.toMap(Menu::getId, menu -> menu));
        Set<String> permissionCodes = new HashSet<>();
        roles.forEach(role -> {
            roleRepository.getAssignedMenus(role.getId()).forEach(menuId -> {
                Menu menu = menuMap.get(menuId);
                if (menu != null
                        && menu.getPermissionCode() != null
                        && !menu.getPermissionCode().isBlank()) {
                    permissionCodes.add(menu.getPermissionCode());
                }
            });
            permissionCodes.addAll(roleRepository.getAssignedResources(role.getId()));
        });
        return Set.copyOf(permissionCodes);
    }

    private Set<DepartmentId> loadUserDepartmentIds(UserId userId) {
        Set<DepartmentId> departmentIds = new HashSet<>();
        roleRepository
                .findRolesByUserId(userId)
                .forEach(role -> departmentIds.addAll(roleRepository.getAssignedDataScopeDepartments(role.getId())));
        return Set.copyOf(departmentIds);
    }

    private Set<String> loadUserScopeTypes(UserId userId) {
        Set<String> scopeTypes = new HashSet<>();
        roleRepository
                .findRolesByUserId(userId)
                .forEach(role -> scopeTypes.add(
                        roleRepository.getAssignedDataScopeType(role.getId()).value()));
        return Set.copyOf(scopeTypes);
    }

    private List<Menu> buildMenuTree(List<Menu> flatMenus) {
        Map<MenuId, Menu> menuMap = new HashMap<>();
        flatMenus.stream()
                .sorted(Comparator.comparing(Menu::getSort)
                        .thenComparing(menu -> menu.getId().value()))
                .forEach(menu -> menuMap.put(
                        menu.getId(),
                        Menu.create(
                                menu.getId(),
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

    private TenantId requireTenantId() {
        return TenantId.of(BaconContextHolder.requireTenantId());
    }
}
