package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.context.BaconIdContextHelper;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.Menu;
import com.github.thundax.bacon.upms.domain.model.entity.Role;
import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.ResourceCode;
import com.github.thundax.bacon.upms.domain.repository.MenuRepository;
import com.github.thundax.bacon.upms.domain.repository.PermissionRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleDataScopeRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleMenuRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleRepository;
import com.github.thundax.bacon.upms.domain.repository.RoleResourceRepository;
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

    private final MenuRepository menuRepository;
    private final RoleRepository roleRepository;
    private final RoleMenuRepository roleMenuRepository;
    private final RoleResourceRepository roleResourceRepository;
    private final RoleDataScopeRepository roleDataScopeRepository;
    private final UpmsPermissionCacheSupport cacheSupport;

    public PermissionRepositoryImpl(
            MenuRepository menuRepository,
            RoleRepository roleRepository,
            RoleMenuRepository roleMenuRepository,
            RoleResourceRepository roleResourceRepository,
            RoleDataScopeRepository roleDataScopeRepository,
            UpmsPermissionCacheSupport cacheSupport) {
        this.menuRepository = menuRepository;
        this.roleRepository = roleRepository;
        this.roleMenuRepository = roleMenuRepository;
        this.roleResourceRepository = roleResourceRepository;
        this.roleDataScopeRepository = roleDataScopeRepository;
        this.cacheSupport = cacheSupport;
    }

    @Override
    public List<Menu> list() {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        return cacheSupport.getTenantMenuTree(tenantId, () -> buildMenuTree(menuRepository.list()));
    }

    @Override
    public List<Menu> listMenuTreeByUserId(UserId userId) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        return cacheSupport.listMenuTreeByUserId(tenantId, userId, () -> loadUserMenuTree(userId));
    }

    @Override
    public Set<String> findPermissionCodesByUserId(UserId userId) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        return cacheSupport.findPermissionCodesByUserId(tenantId, userId, () -> loadUserPermissionCodes(userId));
    }

    @Override
    public Set<DepartmentId> findDepartmentIdsByUserId(UserId userId) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        return cacheSupport.findDepartmentIdsByUserId(tenantId, userId, () -> loadUserDepartmentIds(userId));
    }

    @Override
    public Set<String> findScopeTypesByUserId(UserId userId) {
        TenantId tenantId = BaconIdContextHelper.requireTenantId();
        return cacheSupport.findScopeTypesByUserId(tenantId, userId, () -> loadUserScopeTypes(userId));
    }

    @Override
    public boolean existsAllAccessByUserId(UserId userId) {
        return findScopeTypesByUserId(userId).contains("ALL");
    }

    private List<Menu> loadUserMenuTree(UserId userId) {
        List<Role> roles = roleRepository.findByUserId(userId);
        if (roles.isEmpty()) {
            return List.of();
        }
        Set<MenuId> menuIds = new HashSet<>();
        roles.forEach(role -> menuIds.addAll(roleMenuRepository.findMenuIds(role.getId())));
        if (menuIds.isEmpty()) {
            return List.of();
        }
        List<Menu> menus = menuRepository.list().stream()
                .filter(menu -> menuIds.contains(menu.getId()))
                .toList();
        return buildMenuTree(menus);
    }

    private Set<String> loadUserPermissionCodes(UserId userId) {
        List<Role> roles = roleRepository.findByUserId(userId);
        if (roles.isEmpty()) {
            return Set.of();
        }
        Map<MenuId, Menu> menuMap =
                menuRepository.list().stream().collect(Collectors.toMap(Menu::getId, menu -> menu));
        Set<String> permissionCodes = new HashSet<>();
        roles.forEach(role -> {
            roleMenuRepository.findMenuIds(role.getId()).forEach(menuId -> {
                Menu menu = menuMap.get(menuId);
                if (menu != null
                        && menu.getPermissionCode() != null
                        && !menu.getPermissionCode().isBlank()) {
                    permissionCodes.add(menu.getPermissionCode());
                }
            });
            roleResourceRepository.findResourceCodes(role.getId()).stream()
                    .map(ResourceCode::value)
                    .forEach(permissionCodes::add);
        });
        return Set.copyOf(permissionCodes);
    }

    private Set<DepartmentId> loadUserDepartmentIds(UserId userId) {
        Set<DepartmentId> departmentIds = new HashSet<>();
        roleRepository.findByUserId(userId).forEach(role -> departmentIds.addAll(
                roleDataScopeRepository.findDataScopeDepartmentIds(role.getId())));
        return Set.copyOf(departmentIds);
    }

    private Set<String> loadUserScopeTypes(UserId userId) {
        Set<String> scopeTypes = new HashSet<>();
        roleRepository.findByUserId(userId).stream()
                .map(Role::getId)
                .map(roleDataScopeRepository::findDataScopeType)
                .map(RoleDataScopeType::value)
                .forEach(scopeTypes::add);
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
                        menuMap.get(menu.getParentId()).addChild(menu);
                    }
                });
        return roots;
    }

}
